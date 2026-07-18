package com.fiapx.identity.application.dto;

public record AuthResult(AccessToken accessToken, String refreshToken) {}
