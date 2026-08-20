package com.itda.domain.translation.service;

import com.itda.domain.document.entity.Document;
import com.itda.domain.document.entity.DocumentVersion;
import com.itda.domain.document.repository.DocumentRepository;
import com.itda.domain.document.repository.DocumentVersionRepository;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.domain.translation.dto.request.TranslateRequest;
import com.itda.domain.translation.dto.response.TranslationJobResponse;
import com.itda.domain.translation.entity.TranslationJob;
import com.itda.domain.translation.entity.TranslationLanguage;
import com.itda.domain.translation.repository.TranslationJobRepository;
import com.itda.domain.translation.repository.TranslationLanguageRepository;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.ForbiddenException;
import com.itda.global.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationJobRepository translationJobRepository;
    private final TranslationLanguageRepository translationLanguageRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TranslationAsyncExecutor translationAsyncExecutor;

    @Transactional
    public TranslationJobResponse requestTranslation(Long userId, Long documentId,
                                                      Integer versionNumber,
                                                      TranslateRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        verifyLeader(document.getTeamProject().getId(), userId);

        DocumentVersion documentVersion = documentVersionRepository
                .findByDocument_IdAndVersion(documentId, versionNumber)
                .orElseThrow(() -> new NotFoundException("해당 버전을 찾을 수 없습니다."));

        TranslationJob job = TranslationJob.builder()
                .documentVersion(documentVersion)
                .status("PENDING")
                .totalLanguages(request.translations().size())
                .build();
        translationJobRepository.save(job);

        List<TranslationLanguage> languages = new ArrayList<>();
        for (TranslateRequest.TranslationTarget target : request.translations()) {
            User targetUser = userRepository.findById(target.userId())
                    .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

            TranslationLanguage tl = TranslationLanguage.builder()
                    .translationJob(job)
                    .targetLanguage(target.targetLanguage())
                    .targetUser(targetUser)
                    .build();
            translationLanguageRepository.save(tl);
            languages.add(tl);
        }

        // 별도 빈(TranslationAsyncExecutor)을 통해 호출해야 @Async 프록시가 동작함
        translationAsyncExecutor.executeTranslation(job.getId(), documentVersion.getId(),
                document.getLanguage());

        return TranslationJobResponse.from(job, languages);
    }

    @Transactional(readOnly = true)
    public TranslationJobResponse getJobStatus(Long userId, Long jobId) {
        TranslationJob job = translationJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("번역 작업을 찾을 수 없습니다."));

        Long teamId = job.getDocumentVersion().getDocument().getTeamProject().getId();
        verifyTeamMember(teamId, userId);

        List<TranslationLanguage> languages =
                translationLanguageRepository.findByTranslationJob_Id(jobId);

        return TranslationJobResponse.from(job, languages);
    }

    public String getChannelName(Long jobId) {
        return "translation:" + jobId;
    }

    private void verifyLeader(Long teamId, Long userId) {
        var member = teamMemberRepository.findByTeamProject_IdAndUser_Id(teamId, userId)
                .orElseThrow(() -> new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다."));

        if (!"LEADER".equals(member.getRole())) {
            throw new ForbiddenException("팀장만 수행할 수 있는 작업입니다.");
        }
    }

    private void verifyTeamMember(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamProject_IdAndUser_Id(teamId, userId)) {
            throw new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다.");
        }
    }
}
