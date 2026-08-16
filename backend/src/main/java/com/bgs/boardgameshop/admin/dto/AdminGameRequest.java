package com.bgs.boardgameshop.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AdminGameRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        String category,
        String imageUrl,
        String publisher,
        Integer minPlayers,
        Integer maxPlayers,
        Integer durationMinutes,
        @NotNull @Min(0) Integer stock,
        Integer minAge,
        LocalDate releaseDate,
        @Min(1) @Max(100) Integer discountPercent,
        Instant discountEndsAt,
        List<Long> tagIds
) {
}
