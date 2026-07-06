package com.hirehub.mapper;

import com.hirehub.dto.user.UserResponse;
import com.hirehub.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isEnabled());
    }
}
