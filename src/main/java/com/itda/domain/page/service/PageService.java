package com.itda.domain.page.service;

import com.itda.domain.document.entity.DocumentVersion;
import com.itda.domain.document.repository.DocumentVersionRepository;
import com.itda.domain.page.dto.request.CreatePageRequest;
import com.itda.domain.page.dto.request.ReorderPagesRequest;
import com.itda.domain.page.dto.request.UpdatePageRequest;
import com.itda.domain.page.dto.response.PageResponse;
import com.itda.domain.page.entity.Page;
import com.itda.domain.page.repository.PageRepository;
import com.itda.domain.pin.entity.Pin;
import com.itda.domain.pin.repository.PinRepository;
import com.itda.domain.requirement.repository.RequirementRepository;
import com.itda.domain.team.entity.TeamMember;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.global.error.ForbiddenException;
import com.itda.global.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageService {

    private final PageRepository pageRepository;
    private final PinRepository pinRepository;
    private final RequirementRepository requirementRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public PageResponse createPage(Long userId, Long documentId, Integer version, CreatePageRequest request) {
        DocumentVersion documentVersion = findDocumentVersion(documentId, version);
        verifyLeader(documentVersion, userId);

        List<Page> existingPages = pageRepository
                .findByDocumentVersion_IdOrderByPageNumberAsc(documentVersion.getId());
        int nextPageNumber = existingPages.isEmpty() ? 1 : existingPages.size() + 1;

        Page page = Page.builder()
                .documentVersion(documentVersion)
                .pageNumber(nextPageNumber)
                .screenName(request.screenName())
                .screenId(request.screenId())
                .build();
        pageRepository.save(page);

        return PageResponse.from(page);
    }

    @Transactional
    public PageResponse updatePage(Long userId, Long pageId, UpdatePageRequest request) {
        Page page = findPage(pageId);
        verifyLeader(page.getDocumentVersion(), userId);

        page.update(request.screenName(), request.screenId());

        return PageResponse.from(page);
    }

    @Transactional
    public void deletePage(Long userId, Long pageId) {
        Page page = findPage(pageId);
        verifyLeader(page.getDocumentVersion(), userId);

        List<Pin> pins = pinRepository.findByPage_IdOrderByPinNumberAsc(pageId);
        for (Pin pin : pins) {
            requirementRepository.deleteByPin_Id(pin.getId());
        }
        pinRepository.deleteByPage_Id(pageId);
        pageRepository.delete(page);

        // 남은 페이지 번호 재정렬
        List<Page> remainingPages = pageRepository
                .findByDocumentVersion_IdOrderByPageNumberAsc(page.getDocumentVersion().getId());
        for (int i = 0; i < remainingPages.size(); i++) {
            remainingPages.get(i).updatePageNumber(i + 1);
        }
    }

    @Transactional
    public void reorderPages(Long userId, Long documentId, Integer version, ReorderPagesRequest request) {
        DocumentVersion documentVersion = findDocumentVersion(documentId, version);
        verifyLeader(documentVersion, userId);

        List<Long> pageIds = request.pageIds();
        for (int i = 0; i < pageIds.size(); i++) {
            Page page = findPage(pageIds.get(i));
            page.updatePageNumber(i + 1);
        }
    }

    private DocumentVersion findDocumentVersion(Long documentId, Integer version) {
        return documentVersionRepository.findByDocument_IdAndVersion(documentId, version)
                .orElseThrow(() -> new NotFoundException("해당 버전을 찾을 수 없습니다."));
    }

    private Page findPage(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new NotFoundException("페이지를 찾을 수 없습니다."));
    }

    private void verifyLeader(DocumentVersion documentVersion, Long userId) {
        Long teamProjectId = documentVersion.getDocument().getTeamProject().getId();
        TeamMember member = teamMemberRepository.findByTeamProject_IdAndUser_Id(teamProjectId, userId)
                .orElseThrow(() -> new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다."));

        if (!"LEADER".equals(member.getRole())) {
            throw new ForbiddenException("팀장만 수행할 수 있는 작업입니다.");
        }
    }
}
