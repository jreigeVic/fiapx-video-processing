package com.fiapx.identity.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank String name, @NotBlank @Email String email, @NotBlank String password) {}
