package com.syn.usermanagement.dto;

import com.syn.usermanagement.entity.User;
import java.io.Serializable;

public record UserResponse(
        Long id,
        String name,
        String email
) implements Serializable {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
