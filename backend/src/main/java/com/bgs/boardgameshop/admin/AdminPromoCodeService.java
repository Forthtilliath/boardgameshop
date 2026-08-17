package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminPromoCodeRequest;
import com.bgs.boardgameshop.admin.dto.AdminPromoCodeResponse;
import com.bgs.boardgameshop.promocode.PromoCode;
import com.bgs.boardgameshop.promocode.PromoCodeAlreadyExistsException;
import com.bgs.boardgameshop.promocode.PromoCodeNotFoundException;
import com.bgs.boardgameshop.promocode.PromoCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public AdminPromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminPromoCodeResponse> getPromoCodes() {
        return promoCodeRepository.findAllByOrderByCodeAsc().stream()
                .map(AdminPromoCodeResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AdminPromoCodeResponse createPromoCode(AdminPromoCodeRequest request) {
        String code = request.code().trim().toUpperCase();
        if (promoCodeRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new PromoCodeAlreadyExistsException(code);
        }
        PromoCode promoCode = PromoCode.builder().code(code).usesCount(0).build();
        applyRequest(promoCode, request);
        return AdminPromoCodeResponse.fromEntity(promoCodeRepository.save(promoCode));
    }

    @Transactional
    public AdminPromoCodeResponse updatePromoCode(Long id, AdminPromoCodeRequest request) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new PromoCodeNotFoundException(id));
        applyRequest(promoCode, request);
        return AdminPromoCodeResponse.fromEntity(promoCodeRepository.save(promoCode));
    }

    @Transactional
    public void deletePromoCode(Long id) {
        if (!promoCodeRepository.existsById(id)) {
            throw new PromoCodeNotFoundException(id);
        }
        promoCodeRepository.deleteById(id);
    }

    private void applyRequest(PromoCode promoCode, AdminPromoCodeRequest request) {
        promoCode.setCode(request.code().trim().toUpperCase());
        promoCode.setDiscountPercent(request.discountPercent());
        promoCode.setActive(request.active());
        promoCode.setExpiresAt(request.expiresAt());
        promoCode.setMaxUses(request.maxUses());
    }
}
