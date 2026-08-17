package com.bgs.boardgameshop.promocode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promo-codes")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    /** Prévisualise la réduction d'un code, avant de passer commande (voir aussi OrderService#createOrder). */
    @PostMapping("/validate")
    public PromoCodeResponse validate(@Valid @RequestBody ValidateRequest request) {
        return promoCodeService.validate(request.code());
    }

    public record ValidateRequest(@NotBlank String code) {
    }
}
