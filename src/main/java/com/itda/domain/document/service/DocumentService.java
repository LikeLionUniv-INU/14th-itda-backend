package com.itda.domain.document.service;

import com.itda.domain.document.dto.request.CreateDocumentRequest;
import com.itda.domain.document.dto.request.CreateVersionRequest;
import com.itda.domain.document.dto.request.SaveDocumentRequest;
import com.itda.domain.document.dto.response.CreateDocumentResponse;
import com.itda.domain.document.dto.response.DocumentDetailResponse;
import com.itda.domain.document.dto.response.DocumentVersionResponse;
import com.itda.domain.document.entity.Document;
import com.itda.domain.document.entity.DocumentVersion;
import com.itda.domain.document.repository.DocumentRepository;
import com.itda.domain.document.repository.DocumentVersionRepository;
import com.itda.domain.page.entity.Page;
import com.itda.domain.page.entity.WireframeImage;
import com.itda.domain.page.repository.PageRepository;
import com.itda.domain.page.repository.WireframeImageRepository;
import com.itda.domain.pin.entity.Pin;
import com.itda.domain.pin.repository.PinRepository;
import com.itda.domain.requirement.entity.Requirement;
import com.itda.domain.requirement.repository.RequirementRepository;
import com.itda.domain.team.entity.ActivityLog;
import com.itda.domain.team.entity.TeamMember;
import com.itda.domain.team.repository.ActivityLogRepository;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.domain.translation.entity.TranslatedRequirement;
import com.itda.domain.translation.entity.TranslationLanguage;
import com.itda.domain.translation.repository.TranslatedRequirementRepository;
import com.itda.domain.translation.repository.TranslationLanguageRepository;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.ForbiddenException;
import com.itda.global.error.NotFoundException;
import com.itda.global.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final PageRepository pageRepository;
    private final WireframeImageRepository wireframeImageRepository;
    private final PinRepository pinRepository;
    private final RequirementRepository requirementRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ChangeTrackingService changeTrackingService;
    private final TranslationLanguageRepository translationLanguageRepository;
    private final TranslatedRequirementRepository translatedRequirementRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateDocumentResponse createDocument(Long userId, Long teamId, CreateDocumentRequest request) {
        User user = findUser(userId);
        verifyLeader(teamId, userId);

        Document document = Document.builder()
                .teamProject(teamMemberRepository.findByTeamProject_IdAndUser_Id(teamId, userId)
                        .orElseThrow(() -> new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다."))
                        .getTeamProject())
                .name(request.name())
                .language(request.language())
                .documentType(request.documentType() != null ? request.documentType() : "STORYBOARD")
                .createdBy(user)
                .build();
        documentRepository.save(document);

        DocumentVersion version = DocumentVersion.builder()
                .document(document)
                .version(request.version())
                .status("DRAFT")
                .isAutoSaved(false)
                .createdBy(user)
                .build();
        documentVersionRepository.save(version);

        activityLogRepository.save(ActivityLog.builder()
                .teamProject(document.getTeamProject())
                .document(document)
                .actionType("UPLOADED")
                .documentName(document.getName())
                .documentType(document.getDocumentType())
                .version(version.getVersion())
                .performedBy(user)
                .build());

        return CreateDocumentResponse.of(document, version);
    }

    public DocumentDetailResponse getDocumentVersion(Long userId, Long documentId,
                                                      Integer versionNumber, String lang) {
        Document document = findDocument(documentId);
        verifyTeamMember(document.getTeamProject().getId(), userId);

        DocumentVersion documentVersion = documentVersionRepository
                .findByDocument_IdAndVersion(documentId, versionNumber)
                .orElseThrow(() -> new NotFoundException("해당 버전을 찾을 수 없습니다."));

        Long translationLanguageId = resolveTranslationLanguageId(documentVersion.getId(), lang);

        List<Page> pages = pageRepository.findByDocumentVersion_IdOrderByPageNumberAsc(documentVersion.getId());

        List<DocumentDetailResponse.PageInfo> pageInfos = pages.stream()
                .map(page -> {
                    List<Pin> pins = pinRepository.findByPage_IdOrderByPinNumberAsc(page.getId());

                    List<DocumentDetailResponse.PinInfo> pinInfos = pins.stream()
                            .map(pin -> {
                                List<Requirement> requirements = requirementRepository.findByPin_Id(pin.getId());

                                List<DocumentDetailResponse.RequirementInfo> reqInfos = requirements.stream()
                                        .map(req -> buildRequirementInfo(req, translationLanguageId))
                                        .toList();

                                return new DocumentDetailResponse.PinInfo(
                                        pin.getId(),
                                        pin.getPinNumber(),
                                        pin.getXCoordinate(),
                                        pin.getYCoordinate(),
                                        reqInfos
                                );
                            })
                            .toList();

                    List<WireframeImage> images = wireframeImageRepository.findByPage_Id(page.getId());
                    List<DocumentDetailResponse.WireframeImageInfo> imageInfos = images.stream()
                            .map(img -> new DocumentDetailResponse.WireframeImageInfo(
                                    img.getId(),
                                    img.getImageType(),
                                    img.getImageUrl(),
                                    img.getOriginalWidth(),
                                    img.getOriginalHeight(),
                                    img.getDisplayWidth(),
                                    img.getDisplayHeight()
                            ))
                            .toList();

                    return new DocumentDetailResponse.PageInfo(
                            page.getId(),
                            page.getPageNumber(),
                            page.getScreenName(),
                            page.getScreenId(),
                            imageInfos,
                            pinInfos
                    );
                })
                .toList();

        return new DocumentDetailResponse(
                document.getId(),
                document.getName(),
                document.getLanguage(),
                document.getDocumentType(),
                documentVersion.getVersion(),
                documentVersion.getStatus(),
                documentVersion.getIsAutoSaved(),
                documentVersion.getChangeSummary(),
                documentVersion.getCreatedAt(),
                documentVersion.getUpdatedAt(),
                pageInfos
        );
    }

    private Long resolveTranslationLanguageId(Long documentVersionId, String lang) {
        if (lang == null || lang.isBlank()) {
            return null;
        }
        List<TranslationLanguage> completedLanguages =
                translationLanguageRepository
                        .findByTranslationJob_DocumentVersion_IdAndTargetLanguageAndStatus(
                                documentVersionId, lang, "COMPLETED");
        if (completedLanguages.isEmpty()) {
            return null;
        }
        return completedLanguages.getLast().getId();
    }

    private DocumentDetailResponse.RequirementInfo buildRequirementInfo(
            Requirement req, Long translationLanguageId) {
        if (translationLanguageId != null) {
            List<TranslatedRequirement> translated =
                    translatedRequirementRepository.findByRequirement_IdAndTranslationLanguage_Id(
                            req.getId(), translationLanguageId);
            if (!translated.isEmpty()) {
                TranslatedRequirement tr = translated.getFirst();
                return new DocumentDetailResponse.RequirementInfo(
                        req.getId(),
                        req.getTabType(),
                        tr.getTranslatedItemName(),
                        tr.getTranslatedContent()
                );
            }
        }
        return new DocumentDetailResponse.RequirementInfo(
                req.getId(),
                req.getTabType(),
                req.getItemName(),
                req.getContent()
        );
    }

    public List<DocumentVersionResponse> getVersionList(Long userId, Long documentId) {
        Document document = findDocument(documentId);
        verifyTeamMember(document.getTeamProject().getId(), userId);

        List<DocumentVersion> versions = documentVersionRepository
                .findByDocument_IdOrderByVersionDesc(documentId);

        return versions.stream()
                .map(DocumentVersionResponse::from)
                .toList();
    }

    @Transactional
    public void saveDocument(Long userId, Long documentId, Integer versionNumber, SaveDocumentRequest request) {
        Document document = findDocument(documentId);
        User user = findUser(userId);
        verifyLeader(document.getTeamProject().getId(), userId);

        DocumentVersion documentVersion = documentVersionRepository
                .findByDocument_IdAndVersion(documentId, versionNumber)
                .orElseThrow(() -> new NotFoundException("해당 버전을 찾을 수 없습니다."));

        documentVersion.updateForSave(
                request.status() != null ? request.status() : "COMPLETED",
                false,
                request.changeSummary()
        );

        changeTrackingService.detectAndRecordChanges(documentVersion, request.pages(), user);

        deleteVersionContent(documentVersion.getId());
        saveVersionContent(documentVersion, request.pages());

        activityLogRepository.save(ActivityLog.builder()
                .teamProject(document.getTeamProject())
                .document(document)
                .actionType("UPDATED")
                .documentName(document.getName())
                .documentType(document.getDocumentType())
                .version(documentVersion.getVersion())
                .performedBy(user)
                .build());
    }

    @Transactional
    public void autoSaveDocument(Long userId, Long documentId, Integer versionNumber, SaveDocumentRequest request) {
        Document document = findDocument(documentId);
        findUser(userId);
        verifyLeader(document.getTeamProject().getId(), userId);

        DocumentVersion documentVersion = documentVersionRepository
                .findByDocument_IdAndVersion(documentId, versionNumber)
                .orElseThrow(() -> new NotFoundException("해당 버전을 찾을 수 없습니다."));

        documentVersion.updateForSave("DRAFT", true, request.changeSummary());

        deleteVersionContent(documentVersion.getId());
        saveVersionContent(documentVersion, request.pages());
    }

    @Transactional
    public DocumentVersionResponse createNewVersion(Long userId, Long documentId, CreateVersionRequest request) {
        Document document = findDocument(documentId);
        User user = findUser(userId);
        verifyLeader(document.getTeamProject().getId(), userId);

        DocumentVersion baseVersion = documentVersionRepository
                .findByDocument_IdAndVersion(documentId, request.baseVersion())
                .orElseThrow(() -> new NotFoundException("기준 버전을 찾을 수 없습니다."));

        DocumentVersion latestVersion = documentVersionRepository
                .findTopByDocument_IdOrderByVersionDesc(documentId)
                .orElseThrow(() -> new NotFoundException("문서 버전을 찾을 수 없습니다."));

        int newVersionNumber = latestVersion.getVersion() + 1;

        DocumentVersion newVersion = DocumentVersion.builder()
                .document(document)
                .version(newVersionNumber)
                .status("EDITING")
                .isAutoSaved(false)
                .createdBy(user)
                .build();
        documentVersionRepository.save(newVersion);

        copyVersionContent(baseVersion.getId(), newVersion);

        activityLogRepository.save(ActivityLog.builder()
                .teamProject(document.getTeamProject())
                .document(document)
                .actionType("UPLOADED")
                .documentName(document.getName())
                .documentType(document.getDocumentType())
                .version(newVersionNumber)
                .performedBy(user)
                .build());

        return DocumentVersionResponse.from(newVersion);
    }

    @Transactional
    public void deleteVersion(Long userId, Long documentId, Integer versionNumber) {
        Document document = findDocument(documentId);
        verifyLeader(document.getTeamProject().getId(), userId);

        int versionCount = documentVersionRepository.countByDocument_Id(documentId);
        if (versionCount <= 1) {
            throw new ValidationException("마지막 남은 버전은 삭제할 수 없습니다.");
        }

        DocumentVersion documentVersion = documentVersionRepository
                .findByDocument_IdAndVersion(documentId, versionNumber)
                .orElseThrow(() -> new NotFoundException("해당 버전을 찾을 수 없습니다."));

        documentVersionRepository.delete(documentVersion);
    }

    private void saveVersionContent(DocumentVersion documentVersion, List<SaveDocumentRequest.PageData> pagesData) {
        if (pagesData == null) return;

        for (SaveDocumentRequest.PageData pageData : pagesData) {
            Page page = Page.builder()
                    .documentVersion(documentVersion)
                    .pageNumber(pageData.pageNumber())
                    .screenName(pageData.screenName())
                    .screenId(pageData.screenId())
                    .build();
            pageRepository.save(page);

            if (pageData.pins() == null) continue;

            for (SaveDocumentRequest.PinData pinData : pageData.pins()) {
                Pin pin = Pin.builder()
                        .page(page)
                        .pinNumber(pinData.pinNumber())
                        .xCoordinate(pinData.xCoordinate())
                        .yCoordinate(pinData.yCoordinate())
                        .build();
                pinRepository.save(pin);

                if (pinData.requirements() == null) continue;

                for (SaveDocumentRequest.RequirementData reqData : pinData.requirements()) {
                    Requirement requirement = Requirement.builder()
                            .pin(pin)
                            .tabType(reqData.tabType())
                            .itemName(reqData.itemName())
                            .content(reqData.content())
                            .build();
                    requirementRepository.save(requirement);
                }
            }
        }
    }

    private void copyVersionContent(Long sourceVersionId, DocumentVersion targetVersion) {
        List<Page> sourcePages = pageRepository.findByDocumentVersion_IdOrderByPageNumberAsc(sourceVersionId);

        for (Page sourcePage : sourcePages) {
            Page newPage = Page.builder()
                    .documentVersion(targetVersion)
                    .pageNumber(sourcePage.getPageNumber())
                    .screenName(sourcePage.getScreenName())
                    .screenId(sourcePage.getScreenId())
                    .build();
            pageRepository.save(newPage);

            List<WireframeImage> sourceImages = wireframeImageRepository.findByPage_Id(sourcePage.getId());
            for (WireframeImage sourceImage : sourceImages) {
                WireframeImage newImage = WireframeImage.builder()
                        .page(newPage)
                        .imageType(sourceImage.getImageType())
                        .imageUrl(sourceImage.getImageUrl())
                        .originalWidth(sourceImage.getOriginalWidth())
                        .originalHeight(sourceImage.getOriginalHeight())
                        .displayWidth(sourceImage.getDisplayWidth())
                        .displayHeight(sourceImage.getDisplayHeight())
                        .build();
                wireframeImageRepository.save(newImage);
            }

            List<Pin> sourcePins = pinRepository.findByPage_IdOrderByPinNumberAsc(sourcePage.getId());

            for (Pin sourcePin : sourcePins) {
                Pin newPin = Pin.builder()
                        .page(newPage)
                        .pinNumber(sourcePin.getPinNumber())
                        .xCoordinate(sourcePin.getXCoordinate())
                        .yCoordinate(sourcePin.getYCoordinate())
                        .build();
                pinRepository.save(newPin);

                List<Requirement> sourceRequirements = requirementRepository.findByPin_Id(sourcePin.getId());

                for (Requirement sourceReq : sourceRequirements) {
                    Requirement newReq = Requirement.builder()
                            .pin(newPin)
                            .originalId(sourceReq.getId())
                            .tabType(sourceReq.getTabType())
                            .itemName(sourceReq.getItemName())
                            .content(sourceReq.getContent())
                            .build();
                    requirementRepository.save(newReq);
                }
            }
        }
    }

    private void deleteVersionContent(Long documentVersionId) {
        List<Page> pages = pageRepository.findByDocumentVersion_IdOrderByPageNumberAsc(documentVersionId);
        for (Page page : pages) {
            List<Pin> pins = pinRepository.findByPage_IdOrderByPinNumberAsc(page.getId());
            for (Pin pin : pins) {
                requirementRepository.deleteByPin_Id(pin.getId());
            }
            pinRepository.deleteByPage_Id(page.getId());
        }
        pageRepository.deleteByDocumentVersion_Id(documentVersionId);
    }

    private void verifyLeader(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeamProject_IdAndUser_Id(teamId, userId)
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

    public Long getDocumentVersionId(Long userId, Long documentId, Integer versionNumber) {
        Document document = findDocument(documentId);
        verifyTeamMember(document.getTeamProject().getId(), userId);

        DocumentVersion documentVersion = documentVersionRepository
                .findByDocument_IdAndVersion(documentId, versionNumber)
                .orElseThrow(() -> new NotFoundException("해당 버전을 찾을 수 없습니다."));

        return documentVersion.getId();
    }

    private Document findDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
