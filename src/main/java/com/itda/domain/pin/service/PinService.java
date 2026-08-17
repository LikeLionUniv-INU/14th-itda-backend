package com.itda.domain.pin.service;

import com.itda.domain.page.entity.Page;
import com.itda.domain.page.repository.PageRepository;
import com.itda.domain.pin.dto.request.CreatePinRequest;
import com.itda.domain.pin.dto.request.UpdatePinRequest;
import com.itda.domain.pin.dto.response.PinDetailResponse;
import com.itda.domain.pin.dto.response.PinResponse;
import com.itda.domain.pin.entity.Pin;
import com.itda.domain.pin.repository.PinRepository;
import com.itda.domain.requirement.entity.Requirement;
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
public class PinService {

    private final PinRepository pinRepository;
    private final PageRepository pageRepository;
    private final RequirementRepository requirementRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public PinResponse createPin(Long userId, Long pageId, CreatePinRequest request) {
        Page page = findPage(pageId);
        verifyLeader(page, userId);

        String tab = (request.tabType() != null && !request.tabType().isBlank())
                ? request.tabType() : "공통";

        int nextPinNumber = pinRepository.countByPage_IdAndTabType(pageId, tab) + 1;

        Pin pin = Pin.builder()
                .page(page)
                .pinNumber(nextPinNumber)
                .xCoordinate(request.xCoordinate())
                .yCoordinate(request.yCoordinate())
                .tabType(tab)
                .build();
        pinRepository.save(pin);

        Requirement defaultReq = Requirement.builder()
                .pin(pin)
                .tabType(tab)
                .itemName("")
                .content("")
                .isRequired(false)
                .build();
        requirementRepository.save(defaultReq);

        return PinResponse.from(pin);
    }

    @Transactional
    public PinResponse updatePin(Long userId, Long pageId, Long pinId, UpdatePinRequest request) {
        Page page = findPage(pageId);
        verifyLeader(page, userId);

        Pin pin = findPin(pinId);

        pin.updatePosition(request.xCoordinate(), request.yCoordinate());

        return PinResponse.from(pin);
    }

    @Transactional
    public void deletePin(Long userId, Long pageId, Long pinId) {
        Page page = findPage(pageId);
        verifyLeader(page, userId);

        Pin pin = findPin(pinId);
        int deletedPinNumber = pin.getPinNumber();
        String deletedTabType = pin.getTabType();

        requirementRepository.deleteByPin_Id(pinId);
        pinRepository.delete(pin);
        pinRepository.flush();

        List<Pin> remainingPins = pinRepository.findByPage_IdAndTabTypeOrderByPinNumberAsc(pageId, deletedTabType);
        for (Pin p : remainingPins) {
            if (p.getPinNumber() > deletedPinNumber) {
                p.updatePinNumber(p.getPinNumber() - 1);
            }
        }
    }

    public List<PinDetailResponse> getPins(Long userId, Long pageId, String tabType) {
        Page page = findPage(pageId);
        verifyTeamMember(page, userId);

        List<Pin> pins = (tabType != null && !tabType.isBlank())
                ? pinRepository.findByPage_IdAndTabTypeOrderByPinNumberAsc(pageId, tabType)
                : pinRepository.findByPage_IdOrderByPinNumberAsc(pageId);

        return pins.stream()
                .map(pin -> {
                    List<Requirement> requirements = requirementRepository.findByPin_Id(pin.getId());

                    List<PinDetailResponse.RequirementInfo> reqInfos = requirements.stream()
                            .map(r -> new PinDetailResponse.RequirementInfo(
                                    r.getId(),
                                    r.getTabType(),
                                    r.getItemName(),
                                    r.getContent(),
                                    r.getIsRequired()
                            ))
                            .toList();

                    return new PinDetailResponse(
                            pin.getId(),
                            pin.getPinNumber(),
                            pin.getTabType(),
                            pin.getXCoordinate(),
                            pin.getYCoordinate(),
                            reqInfos
                    );
                })
                .toList();
    }

    private Page findPage(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new NotFoundException("페이지를 찾을 수 없습니다."));
    }

    private Pin findPin(Long pinId) {
        return pinRepository.findById(pinId)
                .orElseThrow(() -> new NotFoundException("핀을 찾을 수 없습니다."));
    }

    private void verifyLeader(Page page, Long userId) {
        Long teamProjectId = page.getDocumentVersion().getDocument().getTeamProject().getId();
        TeamMember member = teamMemberRepository.findByTeamProject_IdAndUser_Id(teamProjectId, userId)
                .orElseThrow(() -> new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다."));

        if (!"LEADER".equals(member.getRole())) {
            throw new ForbiddenException("팀장만 수행할 수 있는 작업입니다.");
        }
    }

    private void verifyTeamMember(Page page, Long userId) {
        Long teamProjectId = page.getDocumentVersion().getDocument().getTeamProject().getId();
        if (!teamMemberRepository.existsByTeamProject_IdAndUser_Id(teamProjectId, userId)) {
            throw new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다.");
        }
    }
}
