package com.bgs.boardgameshop.promocode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validation et consommation des codes promo. Pas de verrou pessimiste sur
 * {@code usesCount} (contrairement au stock des jeux, voir GameRepository#findByIdForUpdate) :
 * simplification assumée, deux commandes simultanées utilisant la toute dernière
 * utilisation autorisée d'un même code pourraient toutes les deux passer. Impact
 * mineur (au pire une utilisation de trop d'un code promo, jamais de perte d'argent
 * réel côté marchand), jugé disproportionné à corriger vu la faible criticité.
 */
@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse validate(String code) {
        return PromoCodeResponse.fromEntity(validateAndGet(code));
    }

    @Transactional(readOnly = true)
    public PromoCode validateAndGet(String code) {
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new PromoCodeInvalidException(code));
        if (!promoCode.isCurrentlyValid()) {
            throw new PromoCodeInvalidException(code);
        }
        return promoCode;
    }

    @Transactional
    public void incrementUsage(PromoCode promoCode) {
        promoCode.setUsesCount(promoCode.getUsesCount() + 1);
        promoCodeRepository.save(promoCode);
    }
}
