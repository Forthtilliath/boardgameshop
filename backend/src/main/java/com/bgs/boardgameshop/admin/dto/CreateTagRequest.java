package com.bgs.boardgameshop.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTagRequest(
        @NotBlank String name
) {
}
