package com.fiapx.video.application.ports.out;

import com.fiapx.video.application.dto.AuthenticatedUser;

public interface TokenValidatorPort {

    AuthenticatedUser validate(String accessToken);
}
