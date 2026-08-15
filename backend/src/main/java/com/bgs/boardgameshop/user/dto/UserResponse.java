package com.bgs.boardgameshop.user.dto;

import com.bgs.boardgameshop.user.User;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }
}
