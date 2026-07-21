package com.fiapx.video.api.controller;

import com.fiapx.video.application.dto.ErrorResponse;
import com.fiapx.video.domain.exception.InvalidUploadException;
import com.fiapx.video.domain.exception.VideoNotFoundException;
import com.fiapx.video.domain.exception.VideoNotReadyForDownloadException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            VideoNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(VideoNotReadyForDownloadException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            VideoNotReadyForDownloadException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidUploadException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidation(
            RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponse body =
                new ErrorResponse(
                        Instant.now(), status.value(), error, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
