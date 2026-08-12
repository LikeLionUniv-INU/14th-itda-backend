package com.itda.domain.translation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itda.domain.document.entity.Document;
import com.itda.domain.document.entity.DocumentVersion;
import com.itda.domain.document.repository.DocumentRepository;
import com.itda.domain.document.repository.DocumentVersionRepository;
import com.itda.domain.page.entity.Page;
import com.itda.domain.page.repository.PageRepository;
import com.itda.domain.pin.entity.Pin;
import com.itda.domain.pin.repository.PinRepository;
import com.itda.domain.requirement.entity.Requirement;
import com.itda.domain.requirement.repository.RequirementRepository;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.domain.translation.dto.request.TranslateRequest;
import com.itda.domain.translation.dto.response.TranslationJobResponse;
import com.itda.domain.translation.entity.TranslatedRequirement;
import com.itda.domain.translation.entity.TranslationJob;
import com.itda.domain.translation.entity.TranslationLanguage;
import com.itda.domain.translation.repository.TranslatedRequirementRepository;
import com.itda.domain.translation.repository.TranslationJobRepository;
import com.itda.domain.translation.repository.TranslationLanguageRepository;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.ForbiddenException;
import com.itda.global.error.NotFoundException;
import com.itda.infra.ai.AiTranslationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationJobRepository translationJobRepository;
    private final TranslationLanguageRepository translationLanguageRepository;
    private final TranslatedRequirementRepository translatedRequirementRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final PageRepository pageRepository;
    private final PinRepository pinRepository;
    private final RequirementRepository requirementRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AiTranslationClient aiTranslationClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

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

        executeTranslationAsync(job.getId(), documentVersion.getId(),
                document.getLanguage());

        return TranslationJobResponse.from(job, languages);
    }

    @Async
    public void executeTranslationAsync(Long jobId, Long documentVersionId,
                                         String sourceLanguage) {
        try {
            TranslationJob job = translationJobRepository.findById(jobId)
                    .orElseThrow(() -> new NotFoundException("번역 작업을 찾을 수 없습니다."));
            job.updateStatus("TRANSLATING");
            translationJobRepository.save(job);

            List<Requirement> allRequirements = collectRequirements(documentVersionId);

            List<TranslationLanguage> languages =
                    translationLanguageRepository.findByTranslationJob_Id(jobId);

            for (TranslationLanguage tl : languages) {
                try {
                    translateForLanguage(tl, allRequirements, sourceLanguage);

                    tl.markCompleted();
                    translationLanguageRepository.save(tl);

                    job.incrementCompletedLanguages();
                    translationJobRepository.save(job);

                    publishEvent(jobId, Map.of(
                            "event", "language-status",
                            "language", tl.getTargetLanguage(),
                            "status", "COMPLETED",
                            "completedLanguages", job.getCompletedLanguages(),
                            "totalLanguages", job.getTotalLanguages(),
                            "progress", (int) ((job.getCompletedLanguages() * 100.0) / job.getTotalLanguages())
                    ));

                } catch (Exception e) {
                    log.error("언어 {} 번역 실패: {}", tl.getTargetLanguage(), e.getMessage(), e);
                    tl.markFailed();
                    translationLanguageRepository.save(tl);

                    publishEvent(jobId, Map.of(
                            "event", "translation-error",
                            "language", tl.getTargetLanguage(),
                            "message", "번역 중 오류가 발생했습니다."
                    ));
                }
            }

            job = translationJobRepository.findById(jobId).orElseThrow();
            if (job.getCompletedLanguages().equals(job.getTotalLanguages())) {
                job.markCompleted();
                translationJobRepository.save(job);

                DocumentVersion dv = documentVersionRepository.findById(documentVersionId)
                        .orElseThrow();
                dv.updateStatus("TRANSLATED");
                documentVersionRepository.save(dv);

                publishEvent(jobId, Map.of(
                        "event", "translation-complete",
                        "status", "COMPLETED",
                        "progress", 100
                ));
            } else {
                job.markFailed();
                translationJobRepository.save(job);
            }

        } catch (Exception e) {
            log.error("번역 작업 실패 jobId={}: {}", jobId, e.getMessage(), e);
            translationJobRepository.findById(jobId).ifPresent(job -> {
                job.markFailed();
                translationJobRepository.save(job);
            });

            publishEvent(jobId, Map.of(
                    "event", "translation-error",
                    "message", "번역 작업 중 오류가 발생했습니다."
            ));
        }
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

    private void translateForLanguage(TranslationLanguage tl,
                                       List<Requirement> requirements,
                                       String sourceLanguage) {
        tl.markTranslating();
        translationLanguageRepository.save(tl);

        List<AiTranslationClient.TranslationInput> inputs = requirements.stream()
                .map(req -> new AiTranslationClient.TranslationInput(
                        req.getId(), req.getItemName(), req.getContent()))
                .toList();

        List<AiTranslationClient.TranslatedItem> results =
                aiTranslationClient.translate(inputs, sourceLanguage, tl.getTargetLanguage());

        for (AiTranslationClient.TranslatedItem item : results) {
            Requirement requirement = requirements.stream()
                    .filter(r -> r.getId().equals(item.id()))
                    .findFirst()
                    .orElse(null);

            if (requirement != null) {
                TranslatedRequirement tr = TranslatedRequirement.builder()
                        .translationLanguage(tl)
                        .requirement(requirement)
                        .translatedItemName(item.itemName())
                        .translatedContent(item.content())
                        .build();
                translatedRequirementRepository.save(tr);
            }
        }
    }

    private List<Requirement> collectRequirements(Long documentVersionId) {
        List<Requirement> allRequirements = new ArrayList<>();
        List<Page> pages = pageRepository.findByDocumentVersion_IdOrderByPageNumberAsc(documentVersionId);

        for (Page page : pages) {
            List<Pin> pins = pinRepository.findByPage_IdOrderByPinNumberAsc(page.getId());
            for (Pin pin : pins) {
                allRequirements.addAll(requirementRepository.findByPin_Id(pin.getId()));
            }
        }
        return allRequirements;
    }

    private void publishEvent(Long jobId, Map<String, Object> eventData) {
        try {
            String message = objectMapper.writeValueAsString(eventData);
            redisTemplate.convertAndSend(getChannelName(jobId), message);
        } catch (Exception e) {
            log.error("Redis 이벤트 발행 실패: {}", e.getMessage());
        }
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
