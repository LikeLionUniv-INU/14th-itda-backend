package com.itda.domain.team.service;

import com.itda.domain.document.entity.Document;
import com.itda.domain.document.entity.DocumentVersion;
import com.itda.domain.document.repository.DocumentRepository;
import com.itda.domain.document.repository.DocumentVersionRepository;
import com.itda.domain.team.dto.request.CreateTeamRequest;
import com.itda.domain.team.dto.request.JoinTeamRequest;
import com.itda.domain.team.dto.response.*;
import com.itda.domain.team.dto.response.TeamNotificationResponse;
import com.itda.domain.team.entity.ActivityLog;
import com.itda.domain.team.entity.TeamMember;
import com.itda.domain.team.entity.TeamNotification;
import com.itda.domain.team.entity.TeamNotificationRead;
import com.itda.domain.team.entity.TeamProject;
import com.itda.domain.team.repository.ActivityLogRepository;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.domain.team.repository.TeamNotificationReadRepository;
import com.itda.domain.team.repository.TeamNotificationRepository;
import com.itda.domain.team.repository.TeamProjectRepository;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.DuplicateException;
import com.itda.global.error.ForbiddenException;
import com.itda.global.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamProjectRepository teamProjectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final TeamNotificationRepository teamNotificationRepository;
    private final TeamNotificationReadRepository teamNotificationReadRepository;

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public CreateTeamResponse createTeam(Long userId, CreateTeamRequest request) {
        User user = findUser(userId);
        String inviteCode = generateUniqueInviteCode();

        TeamProject teamProject = TeamProject.builder()
                .name(request.name())
                .defaultLanguage(request.defaultLanguage())
                .inviteCode(inviteCode)
                .createdBy(user)
                .build();
        teamProjectRepository.save(teamProject);

        TeamMember leader = TeamMember.builder()
                .teamProject(teamProject)
                .user(user)
                .role("LEADER")
                .build();
        teamMemberRepository.save(leader);

        return CreateTeamResponse.of(teamProject, user);
    }

    @Transactional
    public JoinTeamResponse joinTeam(Long userId, JoinTeamRequest request) {
        User user = findUser(userId);

        TeamProject teamProject = teamProjectRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 초대 코드입니다."));

        if (teamMemberRepository.existsByTeamProject_IdAndUser_Id(teamProject.getId(), userId)) {
            throw new DuplicateException("이미 참여 중인 팀 프로젝트입니다.");
        }

        TeamMember member = TeamMember.builder()
                .teamProject(teamProject)
                .user(user)
                .role("MEMBER")
                .build();
        teamMemberRepository.save(member);

        return JoinTeamResponse.from(member);
    }

    public TeamDetailResponse getTeamDetail(Long userId, Long teamId) {
        TeamProject teamProject = teamProjectRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("팀 프로젝트를 찾을 수 없습니다."));

        TeamMember myMembership = teamMemberRepository.findByTeamProject_IdAndUser_Id(teamId, userId)
                .orElseThrow(() -> new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다."));

        List<TeamMember> members = teamMemberRepository.findByTeamProject_Id(teamId);

        List<TeamDetailResponse.TeamMemberInfo> memberInfos = members.stream()
                .map(m -> new TeamDetailResponse.TeamMemberInfo(
                        m.getUser().getId(),
                        m.getUser().getFirstName(),
                        m.getUser().getLastName(),
                        m.getUser().getLastName().substring(0, 1).toUpperCase(),
                        m.getRole(),
                        m.getUser().getLanguage(),
                        m.getUser().getCountry()
                ))
                .toList();

        List<String> memberLanguages = members.stream()
                .map(m -> m.getUser().getLanguage())
                .distinct()
                .toList();

        List<Document> documents = documentRepository.findByTeamProject_Id(teamId);

        List<TeamDetailResponse.TeamDocumentInfo> documentInfos = documents.stream()
                .map(doc -> {
                    List<DocumentVersion> versions = documentVersionRepository
                            .findByDocument_IdOrderByVersionDesc(doc.getId());

                    int latestVersion = versions.isEmpty() ? 1 : versions.get(0).getVersion();
                    List<Integer> versionNumbers = versions.stream()
                            .map(DocumentVersion::getVersion)
                            .toList();

                    User updatedBy = versions.isEmpty() ? doc.getCreatedBy() : versions.get(0).getCreatedBy();

                    return new TeamDetailResponse.TeamDocumentInfo(
                            doc.getId(),
                            doc.getName(),
                            doc.getDocumentType(),
                            doc.getLanguage(),
                            latestVersion,
                            versionNumbers,
                            doc.getUpdatedAt(),
                            new TeamDetailResponse.UpdatedByInfo(
                                    updatedBy.getFirstName(),
                                    updatedBy.getLastName()
                            )
                    );
                })
                .toList();

        List<ActivityLog> activityLogs = activityLogRepository
                .findTop10ByTeamProject_IdOrderByCreatedAtDesc(teamId);

        List<TeamDetailResponse.ActivityLogInfo> activityLogInfos = activityLogs.stream()
                .map(log -> new TeamDetailResponse.ActivityLogInfo(
                        log.getId(),
                        log.getActionType(),
                        log.getDocumentName(),
                        log.getDocumentType(),
                        log.getVersion(),
                        log.getPerformedBy().getFirstName(),
                        log.getPerformedBy().getLastName(),
                        log.getPerformedBy().getLastName().substring(0, 1).toUpperCase(),
                        log.getCreatedAt()
                ))
                .toList();

        return new TeamDetailResponse(
                teamProject.getId(),
                teamProject.getName(),
                teamProject.getDefaultLanguage(),
                teamProject.getInviteCode(),
                teamProject.getCreatedAt(),
                myMembership.getRole(),
                memberInfos,
                documentInfos,
                memberLanguages,
                activityLogInfos
        );
    }

    public InviteCodeResponse getInviteCode(Long userId, Long teamId) {
        if (!teamMemberRepository.existsByTeamProject_IdAndUser_Id(teamId, userId)) {
            throw new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다.");
        }

        TeamProject teamProject = teamProjectRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("팀 프로젝트를 찾을 수 없습니다."));

        return new InviteCodeResponse(teamProject.getInviteCode());
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = generateInviteCode();
        } while (teamProjectRepository.existsByInviteCode(code));
        return code;
    }

    private String generateInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(random.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }

    public List<TeamNotificationResponse> getUnreadNotifications(Long userId, Long teamId) {
        verifyTeamMember(teamId, userId);

        List<TeamNotification> notifications = teamNotificationRepository
                .findUnreadByTeamAndUser(teamId, userId);

        return notifications.stream()
                .map(TeamNotificationResponse::from)
                .toList();
    }

    @Transactional
    public void readNotification(Long userId, Long teamId, Long notificationId) {
        verifyTeamMember(teamId, userId);

        TeamNotification notification = teamNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("알림을 찾을 수 없습니다."));

        if (!notification.getTeamProject().getId().equals(teamId)) {
            throw new ForbiddenException("해당 팀의 알림이 아닙니다.");
        }

        if (teamNotificationReadRepository.existsByNotification_IdAndUser_Id(notificationId, userId)) {
            return;
        }

        User user = findUser(userId);
        teamNotificationReadRepository.save(TeamNotificationRead.builder()
                .notification(notification)
                .user(user)
                .build());
    }

    private void verifyTeamMember(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamProject_IdAndUser_Id(teamId, userId)) {
            throw new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다.");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
