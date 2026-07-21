package com.fiapx.notification.domain.exception;

/**
 * A permanent, known-safe delivery failure (e.g. the recipient address was rejected). Distinct from
 * an unchecked/unexpected failure, which is left to propagate so the SQS consumer does not ack the
 * message and the notification is retried via redrive/DLQ.
 */
public class NotificationDeliveryException extends RuntimeException {

    public NotificationDeliveryException(String safeReason) {
        super(safeReason);
    }

    public NotificationDeliveryException(String safeReason, Throwable cause) {
        super(safeReason, cause);
    }
}
