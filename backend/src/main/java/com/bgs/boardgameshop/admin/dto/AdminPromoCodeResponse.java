package com.bgs.boardgameshop.admin.dto;

import com.bgs.boardgameshop.promocode.PromoCode;

import java.time.Instant;

public record AdminPromoCodeResponse(
        Long id,
        String code,
        Integer discountPercent,
        boolean active,
        Instant expiresAt,
        Integer maxUses,
        Integer usesCount
) {

    public static AdminPromoCodeResponse fromEntity(PromoCode promoCode) {
        return new AdminPromoCodeResponse(
                promoCode.getId(),
                promoCode.getCode(),
                promoCode.getDiscountPercent(),
                promoCode.isActive(),
                promoCode.getExpiresAt(),
                promoCode.getMaxUses(),
                promoCode.getUsesCount()
        );
    }
}
