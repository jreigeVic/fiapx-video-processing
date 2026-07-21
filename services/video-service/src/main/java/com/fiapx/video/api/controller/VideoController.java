package com.fiapx.video.api.controller;

import com.fiapx.video.api.mapper.VideoMapper;
import com.fiapx.video.api.response.DownloadUrlResponse;
import com.fiapx.video.api.response.VideoResponse;
import com.fiapx.video.api.response.VideoUploadResponse;
import com.fiapx.video.application.dto.AuthenticatedUser;
import com.fiapx.video.application.dto.DownloadUrl;
import com.fiapx.video.application.dto.UploadedFile;
import com.fiapx.video.application.usecase.GenerateDownloadUrlUseCase;
import com.fiapx.video.application.usecase.GetVideoUseCase;
import com.fiapx.video.application.usecase.ListUserVideosUseCase;
import com.fiapx.video.application.usecase.UploadVideoUseCase;
import com.fiapx.video.domain.exception.InvalidUploadException;
import com.fiapx.video.domain.model.Video;
import com.fiapx.video.domain.model.VideoStatus;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final UploadVideoUseCase uploadVideoUseCase;
    private final ListUserVideosUseCase listUserVideosUseCase;
    private final GetVideoUseCase getVideoUseCase;
    private final GenerateDownloadUrlUseCase generateDownloadUrlUseCase;

    public VideoController(
            UploadVideoUseCase uploadVideoUseCase,
            ListUserVideosUseCase listUserVideosUseCase,
            GetVideoUseCase getVideoUseCase,
            GenerateDownloadUrlUseCase generateDownloadUrlUseCase) {
        this.uploadVideoUseCase = uploadVideoUseCase;
        this.listUserVideosUseCase = listUserVideosUseCase;
        this.getVideoUseCase = getVideoUseCase;
        this.generateDownloadUrlUseCase = generateDownloadUrlUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VideoUploadResponse upload(
            Authentication authentication, @RequestPart("file") MultipartFile file) {
        AuthenticatedUser user = currentUser(authentication);
        Video video = uploadVideoUseCase.execute(user.id(), user.email(), toUploadedFile(file));
        return VideoMapper.toUploadResponse(video);
    }

    @GetMapping
    public List<VideoResponse> list(
            Authentication authentication, @RequestParam(required = false) VideoStatus status) {
        AuthenticatedUser user = currentUser(authentication);
        return listUserVideosUseCase.execute(user.id(), status).stream()
                .map(VideoMapper::toResponse)
                .toList();
    }

    @GetMapping("/{videoId}")
    public VideoResponse get(Authentication authentication, @PathVariable UUID videoId) {
        AuthenticatedUser user = currentUser(authentication);
        return VideoMapper.toResponse(getVideoUseCase.execute(user.id(), videoId));
    }

    @GetMapping("/{videoId}/download")
    public DownloadUrlResponse download(Authentication authentication, @PathVariable UUID videoId) {
        AuthenticatedUser user = currentUser(authentication);
        DownloadUrl downloadUrl = generateDownloadUrlUseCase.execute(user.id(), videoId);
        return VideoMapper.toDownloadUrlResponse(videoId, downloadUrl);
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private UploadedFile toUploadedFile(MultipartFile file) {
        try {
            return new UploadedFile(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream());
        } catch (IOException e) {
            throw new InvalidUploadException("Unable to read uploaded file", e);
        }
    }
}
