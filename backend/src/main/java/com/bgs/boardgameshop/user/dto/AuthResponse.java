package com.bgs.boardgameshop.user.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
