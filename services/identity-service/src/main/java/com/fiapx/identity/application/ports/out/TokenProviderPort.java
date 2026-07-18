package com.fiapx.identity.application.ports.out;

import com.fiapx.identity.application.dto.AccessToken;
import com.fiapx.identity.domain.model.User;
import java.util.UUID;

public interface TokenProviderPort {

    AccessToken generateAccessToken(User user);

    UUID validateAndGetUserId(String accessToken);
}
