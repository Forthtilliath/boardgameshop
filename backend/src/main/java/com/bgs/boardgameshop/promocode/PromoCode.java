package com.bgs.boardgameshop.promocode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Un code promo appliquant une réduction en pourcentage au total d'une commande. */
@Entity
@Table(name = "promo_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer discountPercent;

    @Column(nullable = false)
    private boolean active;

    /** Null = pas de date d'expiration. */
    private Instant expiresAt;

    /** Null = pas de limite de nombre d'utilisations. */
    private Integer maxUses;

    @Builder.Default
    @Column(nullable = false)
    private Integer usesCount = 0;

    public boolean isCurrentlyValid() {
        if (!active) {
            return false;
        }
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            return false;
        }
        return maxUses == null || usesCount < maxUses;
    }
}
