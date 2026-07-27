package com.fiapx.identity.application.ports.in;

import com.fiapx.identity.domain.model.User;

public interface RegisterUserPort {

    User execute(String name, String rawEmail, String rawPassword);
}
