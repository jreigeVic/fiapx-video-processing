package com.fiapx.identity.domain.exception;

public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid, revoked or expired");
    }
}
