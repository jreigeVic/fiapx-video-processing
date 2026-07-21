package com.fiapx.processing.domain.exception;

/** A known, safe-to-report processing failure (never wraps a raw stack trace message). */
public class ProcessingFailedException extends RuntimeException {

    public ProcessingFailedException(String safeReason) {
        super(safeReason);
    }

    public ProcessingFailedException(String safeReason, Throwable cause) {
        super(safeReason, cause);
    }
}
