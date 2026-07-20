package com.fiapx.identity.domain.exception;

public class InvalidAccessTokenException extends DomainException {

    public InvalidAccessTokenException() {
        super("Access token is invalid or expired");
    }
}
