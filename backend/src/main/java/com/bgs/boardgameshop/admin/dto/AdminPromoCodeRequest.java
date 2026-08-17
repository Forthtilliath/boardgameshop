package com.bgs.boardgameshop.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AdminPromoCodeRequest(
        @NotBlank String code,
        @NotNull @Min(1) @Max(100) Integer discountPercent,
        boolean active,
        Instant expiresAt,
        @Min(1) Integer maxUses
) {
}
