package com.itda.domain.document.service;

import com.itda.domain.document.dto.request.SaveDocumentRequest;
import com.itda.domain.document.dto.response.ChangeSummaryResponse;
import com.itda.domain.document.entity.ChangeConfirmation;
import com.itda.domain.document.entity.DocumentChange;
import com.itda.domain.document.entity.DocumentVersion;
import com.itda.domain.document.repository.ChangeConfirmationRepository;
import com.itda.domain.document.repository.DocumentChangeRepository;
import com.itda.domain.page.entity.Page;
import com.itda.domain.page.repository.PageRepository;
import com.itda.domain.pin.entity.Pin;
import com.itda.domain.pin.repository.PinRepository;
import com.itda.domain.requirement.entity.Requirement;
import com.itda.domain.requirement.repository.RequirementRepository;
import com.itda.domain.translation.entity.TranslatedRequirement;
import com.itda.domain.translation.entity.TranslationLanguage;
import com.itda.domain.translation.repository.TranslatedRequirementRepository;
import com.itda.domain.translation.repository.TranslationLanguageRepository;
import com.itda.domain.user.entity.User;
import com.itda.domain.user.repository.UserRepository;
import com.itda.global.error.DuplicateException;
import com.itda.global.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChangeTrackingService {

    private final DocumentChangeRepository documentChangeRepository;
    private final ChangeConfirmationRepository changeConfirmationRepository;
    private final PageRepository pageRepository;
    private final PinRepository pinRepository;
    private final RequirementRepository requirementRepository;
    private final TranslatedRequirementRepository translatedRequirementRepository;
    private final TranslationLanguageRepository translationLanguageRepository;
    private final UserRepository userRepository;

    @Transactional
    public void detectAndRecordChanges(DocumentVersion documentVersion,
                                        List<SaveDocumentRequest.PageData> newPages,
                                        User modifiedBy) {
        if (newPages == null) return;

        List<Page> oldPages = pageRepository.findByDocumentVersion_IdOrderByPageNumberAsc(documentVersion.getId());

        // 기존 페이지가 없으면 (최초 저장) 변경사항 추적하지 않음
        if (oldPages.isEmpty()) return;

        // 기존 변경사항 삭제 (재저장 시 새로 계산)
        documentChangeRepository.deleteByDocumentVersion_Id(documentVersion.getId());

        Map<Integer, Page> oldPageMap = oldPages.stream()
                .collect(Collectors.toMap(Page::getPageNumber, p -> p));

        Set<Integer> newPageNumbers = newPages.stream()
                .map(SaveDocumentRequest.PageData::pageNumber)
                .collect(Collectors.toSet());

        List<DocumentChange> changes = new ArrayList<>();

        // 새 페이지 데이터 순회
        for (SaveDocumentRequest.PageData newPage : newPages) {
            Page oldPage = oldPageMap.get(newPage.pageNumber());

            if (oldPage == null) {
                // 새로 추가된 페이지 → 모든 요구사항이 ADDED
                addRequirementChangesForNewPage(changes, documentVersion, newPage, modifiedBy);
                continue;
            }

            // 화면 정보 변경 감지
            detectScreenChange(changes, documentVersion, oldPage, newPage, modifiedBy);

            // 핀/요구사항 변경 감지
            detectPinChanges(changes, documentVersion, oldPage, newPage, modifiedBy);
        }

        // 삭제된 페이지
        for (Page oldPage : oldPages) {
            if (!newPageNumbers.contains(oldPage.getPageNumber())) {
                addRequirementChangesForDeletedPage(changes, documentVersion, oldPage, modifiedBy);
            }
        }

        if (!changes.isEmpty()) {
            documentChangeRepository.saveAll(changes);
        }
    }

    private void detectScreenChange(List<DocumentChange> changes, DocumentVersion dv,
                                     Page oldPage, SaveDocumentRequest.PageData newPage, User user) {
        boolean nameChanged = !Objects.equals(oldPage.getScreenName(), newPage.screenName());
        boolean idChanged = !Objects.equals(oldPage.getScreenId(), newPage.screenId());

        if (nameChanged || idChanged) {
            changes.add(DocumentChange.builder()
                    .documentVersion(dv)
                    .changeType("SCREEN_MODIFIED")
                    .pageNumber(newPage.pageNumber())
                    .screenName(newPage.screenName())
                    .itemDescription(oldPage.getScreenName() + " → " + newPage.screenName())
                    .beforeValue(toScreenJson(oldPage.getScreenName(), oldPage.getScreenId()))
                    .afterValue(toScreenJson(newPage.screenName(), newPage.screenId()))
                    .modifiedBy(user)
                    .build());
        }
    }

    private void detectPinChanges(List<DocumentChange> changes, DocumentVersion dv,
                                   Page oldPage, SaveDocumentRequest.PageData newPage, User user) {
        List<Pin> oldPins = pinRepository.findByPage_IdOrderByPinNumberAsc(oldPage.getId());
        Map<String, Pin> oldPinMap = oldPins.stream()
                .collect(Collectors.toMap(p -> p.getTabType() + ":" + p.getPinNumber(), p -> p));

        List<SaveDocumentRequest.PinData> newPins = newPage.pins() != null ? newPage.pins() : List.of();
        Set<String> newPinKeys = newPins.stream()
                .map(p -> (p.tabType() != null ? p.tabType() : "공통") + ":" + p.pinNumber())
                .collect(Collectors.toSet());

        for (SaveDocumentRequest.PinData newPin : newPins) {
            String pinKey = (newPin.tabType() != null ? newPin.tabType() : "공통") + ":" + newPin.pinNumber();
            Pin oldPin = oldPinMap.get(pinKey);

            if (oldPin == null) {
                // 새 핀의 모든 요구사항 = ADDED
                if (newPin.requirements() != null) {
                    for (SaveDocumentRequest.RequirementData req : newPin.requirements()) {
                        changes.add(buildReqChange(dv, "REQUIREMENT_ADDED", newPage.pageNumber(),
                                newPage.screenName(), newPin.pinNumber(), req.itemName(),
                                null, toReqJson(req.tabType(), req.itemName(), req.content()), user));
                    }
                }
                continue;
            }

            // 기존 핀 → 요구사항 비교
            List<Requirement> oldReqs = requirementRepository.findByPin_Id(oldPin.getId());
            List<SaveDocumentRequest.RequirementData> newReqs = newPin.requirements() != null
                    ? newPin.requirements() : List.of();

            int maxLen = Math.max(oldReqs.size(), newReqs.size());
            for (int i = 0; i < maxLen; i++) {
                if (i >= oldReqs.size()) {
                    // 추가된 요구사항
                    SaveDocumentRequest.RequirementData nr = newReqs.get(i);
                    changes.add(buildReqChange(dv, "REQUIREMENT_ADDED", newPage.pageNumber(),
                            newPage.screenName(), newPin.pinNumber(), nr.itemName(),
                            null, toReqJson(nr.tabType(), nr.itemName(), nr.content()), user));
                } else if (i >= newReqs.size()) {
                    // 삭제된 요구사항
                    Requirement or = oldReqs.get(i);
                    changes.add(buildReqChange(dv, "REQUIREMENT_DELETED", newPage.pageNumber(),
                            newPage.screenName(), newPin.pinNumber(), or.getItemName(),
                            toReqJson(or.getTabType(), or.getItemName(), or.getContent()), null, user));
                } else {
                    // 수정 여부 비교
                    Requirement or = oldReqs.get(i);
                    SaveDocumentRequest.RequirementData nr = newReqs.get(i);

                    if (!Objects.equals(or.getTabType(), nr.tabType())
                            || !Objects.equals(or.getItemName(), nr.itemName())
                            || !Objects.equals(or.getContent(), nr.content())) {
                        changes.add(buildReqChange(dv, "REQUIREMENT_MODIFIED", newPage.pageNumber(),
                                newPage.screenName(), newPin.pinNumber(), nr.itemName(),
                                toReqJson(or.getTabType(), or.getItemName(), or.getContent()),
                                toReqJson(nr.tabType(), nr.itemName(), nr.content()), user));
                    }
                }
            }
        }

        // 삭제된 핀
        for (Pin oldPin : oldPins) {
            String oldPinKey = oldPin.getTabType() + ":" + oldPin.getPinNumber();
            if (!newPinKeys.contains(oldPinKey)) {
                List<Requirement> deletedReqs = requirementRepository.findByPin_Id(oldPin.getId());
                for (Requirement or : deletedReqs) {
                    changes.add(buildReqChange(dv, "REQUIREMENT_DELETED", oldPage.getPageNumber(),
                            oldPage.getScreenName(), oldPin.getPinNumber(), or.getItemName(),
                            toReqJson(or.getTabType(), or.getItemName(), or.getContent()), null, user));
                }
            }
        }
    }

    private void addRequirementChangesForNewPage(List<DocumentChange> changes, DocumentVersion dv,
                                                  SaveDocumentRequest.PageData newPage, User user) {
        if (newPage.pins() == null) return;
        for (SaveDocumentRequest.PinData pin : newPage.pins()) {
            if (pin.requirements() == null) continue;
            for (SaveDocumentRequest.RequirementData req : pin.requirements()) {
                changes.add(buildReqChange(dv, "REQUIREMENT_ADDED", newPage.pageNumber(),
                        newPage.screenName(), pin.pinNumber(), req.itemName(),
                        null, toReqJson(req.tabType(), req.itemName(), req.content()), user));
            }
        }
    }

    private void addRequirementChangesForDeletedPage(List<DocumentChange> changes, DocumentVersion dv,
                                                      Page oldPage, User user) {
        List<Pin> pins = pinRepository.findByPage_IdOrderByPinNumberAsc(oldPage.getId());
        for (Pin pin : pins) {
            List<Requirement> reqs = requirementRepository.findByPin_Id(pin.getId());
            for (Requirement req : reqs) {
                changes.add(buildReqChange(dv, "REQUIREMENT_DELETED", oldPage.getPageNumber(),
                        oldPage.getScreenName(), pin.getPinNumber(), req.getItemName(),
                        toReqJson(req.getTabType(), req.getItemName(), req.getContent()), null, user));
            }
        }
    }

    private DocumentChange buildReqChange(DocumentVersion dv, String changeType,
                                           int pageNumber, String screenName, int pinNumber,
                                           String itemName, String beforeValue, String afterValue, User user) {
        return DocumentChange.builder()
                .documentVersion(dv)
                .changeType(changeType)
                .pageNumber(pageNumber)
                .screenName(screenName)
                .pinNumber(pinNumber)
                .itemDescription(itemName)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .modifiedBy(user)
                .build();
    }

    // --- 조회 ---

    @Transactional(readOnly = true)
    public ChangeSummaryResponse getChangeSummary(Long documentVersionId, Long userId, String lang) {
        List<DocumentChange> changes = documentChangeRepository
                .findByDocumentVersion_IdOrderByPageNumberAscPinNumberAsc(documentVersionId);

        Set<Long> confirmedChangeIds = changeConfirmationRepository
                .findByDocumentChange_DocumentVersion_IdAndConfirmedBy_Id(documentVersionId, userId)
                .stream()
                .map(cc -> cc.getDocumentChange().getId())
                .collect(Collectors.toSet());

        // 번역 매핑 구축 (lang이 있을 때만)
        Map<Long, TranslatedRequirement> translationMap = buildTranslationMap(documentVersionId, lang);

        List<ChangeSummaryResponse.ChangeInfo> changeInfos = changes.stream()
                .map(c -> new ChangeSummaryResponse.ChangeInfo(
                        c.getId(),
                        c.getChangeType(),
                        c.getPageNumber(),
                        c.getScreenName(),
                        c.getPinNumber(),
                        translateItemDescription(c.getItemDescription(), c, translationMap),
                        translateChangeValue(c.getBeforeValue(), translationMap),
                        translateChangeValue(c.getAfterValue(), translationMap),
                        c.getModifiedBy().getFirstName(),
                        c.getModifiedBy().getLastName(),
                        c.getCreatedAt(),
                        confirmedChangeIds.contains(c.getId())
                ))
                .toList();

        int total = changeInfos.size();
        int confirmed = (int) changeInfos.stream().filter(ChangeSummaryResponse.ChangeInfo::confirmedByMe).count();

        return new ChangeSummaryResponse(total, confirmed, total - confirmed, changeInfos);
    }

    private Map<Long, TranslatedRequirement> buildTranslationMap(Long documentVersionId, String lang) {
        if (lang == null || lang.isBlank()) {
            return Map.of();
        }
        Long translationLanguageId = translationLanguageRepository
                .findTopByTranslationJob_DocumentVersion_IdAndTargetLanguageAndStatusOrderByIdDesc(
                        documentVersionId, lang, "COMPLETED")
                .map(TranslationLanguage::getId)
                .orElse(null);
        if (translationLanguageId == null) {
            return Map.of();
        }
        List<TranslatedRequirement> allTranslated =
                translatedRequirementRepository.findByTranslationLanguage_Id(translationLanguageId);
        Map<Long, TranslatedRequirement> map = new HashMap<>();
        for (TranslatedRequirement tr : allTranslated) {
            map.put(tr.getRequirement().getId(), tr);
        }
        return map;
    }

    private String translateItemDescription(String original, DocumentChange change,
                                             Map<Long, TranslatedRequirement> translationMap) {
        if (translationMap.isEmpty()) return original;
        // afterValue에서 requirementId를 찾아서 번역된 itemName 사용
        String afterValue = change.getAfterValue();
        if (afterValue == null) afterValue = change.getBeforeValue();
        if (afterValue == null) return original;

        for (TranslatedRequirement tr : translationMap.values()) {
            if (Objects.equals(tr.getRequirement().getItemName(), original)) {
                return tr.getTranslatedItemName();
            }
        }
        return original;
    }

    private String translateChangeValue(String jsonValue, Map<Long, TranslatedRequirement> translationMap) {
        if (jsonValue == null || translationMap.isEmpty()) return jsonValue;

        // JSON에서 itemName과 content 추출
        String itemName = extractJsonField(jsonValue, "itemName");
        String content = extractJsonField(jsonValue, "content");
        if (itemName == null && content == null) return jsonValue;

        // 원본 itemName+content로 매칭되는 번역 찾기
        for (TranslatedRequirement tr : translationMap.values()) {
            Requirement req = tr.getRequirement();
            if (Objects.equals(req.getItemName(), itemName) && Objects.equals(req.getContent(), content)) {
                String tabType = extractJsonField(jsonValue, "tabType");
                return toReqJson(tabType, tr.getTranslatedItemName(), tr.getTranslatedContent());
            }
        }
        return jsonValue;
    }

    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start < 0) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    // --- 확인 처리 ---

    @Transactional
    public void confirmChange(Long changeId, Long userId) {
        DocumentChange change = documentChangeRepository.findById(changeId)
                .orElseThrow(() -> new NotFoundException("수정사항을 찾을 수 없습니다."));

        if (changeConfirmationRepository.existsByDocumentChange_IdAndConfirmedBy_Id(changeId, userId)) {
            throw new DuplicateException("이미 확인한 수정사항입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        changeConfirmationRepository.save(ChangeConfirmation.builder()
                .documentChange(change)
                .confirmedBy(user)
                .build());
    }

    @Transactional
    public void confirmAllChanges(Long documentVersionId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        List<DocumentChange> allChanges = documentChangeRepository
                .findByDocumentVersion_IdOrderByPageNumberAscPinNumberAsc(documentVersionId);

        Set<Long> alreadyConfirmed = changeConfirmationRepository
                .findByDocumentChange_DocumentVersion_IdAndConfirmedBy_Id(documentVersionId, userId)
                .stream()
                .map(cc -> cc.getDocumentChange().getId())
                .collect(Collectors.toSet());

        List<ChangeConfirmation> newConfirmations = allChanges.stream()
                .filter(c -> !alreadyConfirmed.contains(c.getId()))
                .map(c -> ChangeConfirmation.builder()
                        .documentChange(c)
                        .confirmedBy(user)
                        .build())
                .toList();

        if (!newConfirmations.isEmpty()) {
            changeConfirmationRepository.saveAll(newConfirmations);
        }
    }

    // --- JSON 헬퍼 ---

    private String toReqJson(String tabType, String itemName, String content) {
        return "{\"tabType\":\"" + esc(tabType) + "\",\"itemName\":\"" + esc(itemName)
                + "\",\"content\":\"" + esc(content) + "\"}";
    }

    private String toScreenJson(String screenName, String screenId) {
        return "{\"screenName\":\"" + esc(screenName) + "\",\"screenId\":\"" + esc(screenId) + "\"}";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
