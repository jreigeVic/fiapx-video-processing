package com.fiapx.identity.application.dto;

public record AccessToken(String value, long expiresInSeconds) {}
