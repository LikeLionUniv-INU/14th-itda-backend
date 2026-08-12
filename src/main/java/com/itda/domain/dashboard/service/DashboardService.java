package com.itda.domain.dashboard.service;

import com.itda.domain.dashboard.dto.response.*;
import com.itda.domain.document.entity.Document;
import com.itda.domain.document.entity.DocumentVersion;
import com.itda.domain.document.repository.DocumentRepository;
import com.itda.domain.document.repository.DocumentVersionRepository;
import com.itda.domain.team.entity.TeamMember;
import com.itda.domain.team.entity.TeamProject;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.domain.user.dto.response.MemberSummary;
import com.itda.domain.user.dto.response.UserSummary;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public DashboardResponse getDashboard(Long userId) {
        User user = findUser(userId);
        UserSummary userSummary = UserSummary.from(user);

        List<TeamMember> myMemberships = teamMemberRepository.findByUser_Id(userId);
        List<DashboardProjectResponse> projects = myMemberships.stream()
                .map(tm -> buildProjectResponse(tm.getTeamProject()))
                .toList();

        List<Long> teamProjectIds = myMemberships.stream()
                .map(tm -> tm.getTeamProject().getId())
                .toList();
        List<DashboardDocumentResponse> recentDocuments = buildDocumentResponses(teamProjectIds);

        return new DashboardResponse(userSummary, projects, recentDocuments);
    }

    public ProjectListResponse getProjects(Long userId) {
        List<TeamMember> myMemberships = teamMemberRepository.findByUser_Id(userId);
        List<DashboardProjectResponse> projects = myMemberships.stream()
                .map(tm -> buildProjectResponse(tm.getTeamProject()))
                .toList();
        return new ProjectListResponse(projects);
    }

    public DocumentListResponse getDocuments(Long userId) {
        List<TeamMember> myMemberships = teamMemberRepository.findByUser_Id(userId);
        List<Long> teamProjectIds = myMemberships.stream()
                .map(tm -> tm.getTeamProject().getId())
                .toList();
        List<DashboardDocumentResponse> documents = buildDocumentResponses(teamProjectIds);
        return new DocumentListResponse(documents);
    }

    private DashboardProjectResponse buildProjectResponse(TeamProject teamProject) {
        List<TeamMember> members = teamMemberRepository.findByTeamProject_Id(teamProject.getId());

        List<MemberSummary> memberSummaries = members.stream()
                .map(m -> MemberSummary.from(m.getUser()))
                .toList();

        List<String> memberLanguages = members.stream()
                .map(m -> m.getUser().getLanguage())
                .distinct()
                .toList();

        List<Document> documents = documentRepository.findByTeamProject_Id(teamProject.getId());

        LocalDateTime lastDocumentUpdatedAt = documents.stream()
                .map(Document::getUpdatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new DashboardProjectResponse(
                teamProject.getId(),
                teamProject.getName(),
                teamProject.getDefaultLanguage(),
                memberLanguages,
                memberSummaries,
                members.size(),
                documents.size(),
                lastDocumentUpdatedAt
        );
    }

    private List<DashboardDocumentResponse> buildDocumentResponses(List<Long> teamProjectIds) {
        if (teamProjectIds.isEmpty()) {
            return List.of();
        }

        List<Document> documents = documentRepository.findByTeamProject_IdIn(teamProjectIds);

        return documents.stream()
                .map(doc -> {
                    int latestVersion = documentVersionRepository
                            .findTopByDocument_IdOrderByVersionDesc(doc.getId())
                            .map(DocumentVersion::getVersion)
                            .orElse(1);

                    return new DashboardDocumentResponse(
                            doc.getId(),
                            doc.getName(),
                            doc.getTeamProject().getId(),
                            doc.getTeamProject().getName(),
                            doc.getLanguage(),
                            doc.getDocumentType(),
                            latestVersion,
                            doc.getUpdatedAt()
                    );
                })
                .sorted(Comparator.comparing(DashboardDocumentResponse::updatedAt).reversed())
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
