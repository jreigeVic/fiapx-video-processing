package com.fiapx.identity.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fiapx.security.cors")
public class CorsProperties {

    // Empty by default (secure-by-default): no cross-origin browser client
    // is allowed until an operator explicitly configures one (e.g. the
    // demo frontend from Epic 013).
    private List<String> allowedOrigins = List.of();

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
