package com.itda.domain.requirement.service;

import com.itda.domain.pin.entity.Pin;
import com.itda.domain.pin.repository.PinRepository;
import com.itda.domain.requirement.dto.request.CreateRequirementRequest;
import com.itda.domain.requirement.dto.request.UpdateRequirementRequest;
import com.itda.domain.requirement.dto.response.RequirementResponse;
import com.itda.domain.requirement.entity.Requirement;
import com.itda.domain.requirement.repository.RequirementRepository;
import com.itda.domain.team.entity.TeamMember;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.global.error.ForbiddenException;
import com.itda.global.error.NotFoundException;
import com.itda.global.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final PinRepository pinRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public RequirementResponse createRequirement(Long userId, Long pinId, CreateRequirementRequest request) {
        Pin pin = findPin(pinId);
        verifyLeader(pin, userId);

        Requirement requirement = Requirement.builder()
                .pin(pin)
                .tabType(request.tabType())
                .itemName(request.itemName())
                .content(request.content())
                .build();
        requirementRepository.save(requirement);

        return RequirementResponse.from(requirement);
    }

    @Transactional
    public RequirementResponse updateRequirement(Long userId, Long pinId, Long requirementId,
                                                  UpdateRequirementRequest request) {
        Pin pin = findPin(pinId);
        verifyLeader(pin, userId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new NotFoundException("요구사항을 찾을 수 없습니다."));

        if (request.content() != null && request.content().isBlank()) {
            throw new ValidationException("수정내용을 입력해주세요.");
        }

        requirement.update(request.itemName(), request.content());

        return RequirementResponse.from(requirement);
    }

    @Transactional
    public void deleteRequirement(Long userId, Long pinId, Long requirementId) {
        Pin pin = findPin(pinId);
        verifyLeader(pin, userId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new NotFoundException("요구사항을 찾을 수 없습니다."));

        requirementRepository.delete(requirement);
    }

    private Pin findPin(Long pinId) {
        return pinRepository.findById(pinId)
                .orElseThrow(() -> new NotFoundException("핀을 찾을 수 없습니다."));
    }

    private void verifyLeader(Pin pin, Long userId) {
        Long teamProjectId = pin.getPage().getDocumentVersion().getDocument().getTeamProject().getId();
        TeamMember member = teamMemberRepository.findByTeamProject_IdAndUser_Id(teamProjectId, userId)
                .orElseThrow(() -> new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다."));

        if (!"LEADER".equals(member.getRole())) {
            throw new ForbiddenException("팀장만 수행할 수 있는 작업입니다.");
        }
    }
}
