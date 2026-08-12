package com.itda.domain.page.service;

import com.itda.domain.page.dto.request.CreateWireframeImageRequest;
import com.itda.domain.page.dto.request.PresignedUrlRequest;
import com.itda.domain.page.dto.request.UpdateWireframeImageRequest;
import com.itda.domain.page.dto.response.PresignedUrlResponse;
import com.itda.domain.page.dto.response.WireframeImageResponse;
import com.itda.domain.page.entity.Page;
import com.itda.domain.page.entity.WireframeImage;
import com.itda.domain.page.repository.PageRepository;
import com.itda.domain.page.repository.WireframeImageRepository;
import com.itda.domain.team.entity.TeamMember;
import com.itda.domain.team.repository.TeamMemberRepository;
import com.itda.global.error.ForbiddenException;
import com.itda.global.error.NotFoundException;
import com.itda.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WireframeImageService {

    private final WireframeImageRepository wireframeImageRepository;
    private final PageRepository pageRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final S3Service s3Service;

    public PresignedUrlResponse generatePresignedUrl(Long userId, PresignedUrlRequest request) {
        Page page = findPage(request.pageId());
        verifyLeader(page, userId);

        Long teamProjectId = page.getDocumentVersion().getDocument().getTeamProject().getId();
        Long documentId = page.getDocumentVersion().getDocument().getId();

        String extension = extractExtension(request.fileName());
        String key = String.format("wireframes/%d/%d/%d/%s_%d.%s",
                teamProjectId, documentId, page.getId(),
                request.imageType().toLowerCase(),
                System.currentTimeMillis(),
                extension);

        String presignedUrl = s3Service.generatePresignedUploadUrl(key, request.contentType());
        String fileUrl = s3Service.getFileUrl(key);

        return new PresignedUrlResponse(presignedUrl, fileUrl, key);
    }

    @Transactional
    public WireframeImageResponse createWireframeImage(Long userId, Long pageId,
                                                        CreateWireframeImageRequest request) {
        Page page = findPage(pageId);
        verifyLeader(page, userId);

        WireframeImage image = WireframeImage.builder()
                .page(page)
                .imageType(request.imageType())
                .imageUrl(request.imageUrl())
                .originalWidth(request.originalWidth())
                .originalHeight(request.originalHeight())
                .displayWidth(request.displayWidth())
                .displayHeight(request.displayHeight())
                .build();
        wireframeImageRepository.save(image);

        return WireframeImageResponse.from(image);
    }

    @Transactional
    public WireframeImageResponse updateWireframeImage(Long userId, Long pageId, Long imageId,
                                                        UpdateWireframeImageRequest request) {
        Page page = findPage(pageId);
        verifyLeader(page, userId);

        WireframeImage image = wireframeImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("이미지를 찾을 수 없습니다."));

        // 기존 이미지 S3 삭제
        String oldKey = s3Service.extractKeyFromUrl(image.getImageUrl());
        s3Service.deleteFile(oldKey);

        image.update(
                request.imageType(),
                request.imageUrl(),
                request.originalWidth(),
                request.originalHeight(),
                request.displayWidth(),
                request.displayHeight()
        );

        return WireframeImageResponse.from(image);
    }

    @Transactional
    public void deleteWireframeImage(Long userId, Long pageId, Long imageId) {
        Page page = findPage(pageId);
        verifyLeader(page, userId);

        WireframeImage image = wireframeImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("이미지를 찾을 수 없습니다."));

        String key = s3Service.extractKeyFromUrl(image.getImageUrl());
        s3Service.deleteFile(key);

        wireframeImageRepository.delete(image);
    }

    private Page findPage(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new NotFoundException("페이지를 찾을 수 없습니다."));
    }

    private void verifyLeader(Page page, Long userId) {
        Long teamProjectId = page.getDocumentVersion().getDocument().getTeamProject().getId();
        TeamMember member = teamMemberRepository.findByTeamProject_IdAndUser_Id(teamProjectId, userId)
                .orElseThrow(() -> new ForbiddenException("해당 팀 프로젝트의 멤버가 아닙니다."));

        if (!"LEADER".equals(member.getRole())) {
            throw new ForbiddenException("팀장만 수행할 수 있는 작업입니다.");
        }
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "png";
        return fileName.substring(lastDot + 1);
    }
}
