package com.bgs.boardgameshop.promocode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private PromoCodeService promoCodeService;

    @Test
    void validateAndGet_rejects_unknown_code() {
        when(promoCodeRepository.findByCodeIgnoreCase("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promoCodeService.validateAndGet("NOPE"))
                .isInstanceOf(PromoCodeInvalidException.class);
    }

    @Test
    void validateAndGet_rejects_inactive_code() {
        PromoCode code = activeCode();
        code.setActive(false);
        when(promoCodeRepository.findByCodeIgnoreCase("BIENVENUE10")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> promoCodeService.validateAndGet("BIENVENUE10"))
                .isInstanceOf(PromoCodeInvalidException.class);
    }

    @Test
    void validateAndGet_rejects_expired_code() {
        PromoCode code = activeCode();
        code.setExpiresAt(Instant.now().minusSeconds(60));
        when(promoCodeRepository.findByCodeIgnoreCase("BIENVENUE10")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> promoCodeService.validateAndGet("BIENVENUE10"))
                .isInstanceOf(PromoCodeInvalidException.class);
    }

    @Test
    void validateAndGet_rejects_code_with_no_uses_left() {
        PromoCode code = activeCode();
        code.setMaxUses(1);
        code.setUsesCount(1);
        when(promoCodeRepository.findByCodeIgnoreCase("BIENVENUE10")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> promoCodeService.validateAndGet("BIENVENUE10"))
                .isInstanceOf(PromoCodeInvalidException.class);
    }

    @Test
    void validateAndGet_accepts_valid_code_with_uses_remaining() {
        PromoCode code = activeCode();
        code.setMaxUses(5);
        code.setUsesCount(3);
        when(promoCodeRepository.findByCodeIgnoreCase("BIENVENUE10")).thenReturn(Optional.of(code));

        PromoCode result = promoCodeService.validateAndGet("BIENVENUE10");

        assertThat(result.getCode()).isEqualTo("BIENVENUE10");
    }

    @Test
    void incrementUsage_increases_uses_count_by_one() {
        PromoCode code = activeCode();
        code.setUsesCount(2);
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        promoCodeService.incrementUsage(code);

        assertThat(code.getUsesCount()).isEqualTo(3);
        verify(promoCodeRepository).save(code);
    }

    private PromoCode activeCode() {
        return PromoCode.builder()
                .id(1L)
                .code("BIENVENUE10")
                .discountPercent(10)
                .active(true)
                .usesCount(0)
                .build();
    }
}
