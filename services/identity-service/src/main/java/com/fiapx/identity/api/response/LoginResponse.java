package com.fiapx.identity.api.response;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, String refreshToken) {
}
