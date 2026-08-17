package com.bgs.boardgameshop.promocode;

public record PromoCodeResponse(String code, Integer discountPercent) {

    public static PromoCodeResponse fromEntity(PromoCode promoCode) {
        return new PromoCodeResponse(promoCode.getCode(), promoCode.getDiscountPercent());
    }
}
