package com.bgs.boardgameshop.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Un jeu de societe du catalogue BGS.
 */
@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    private String imageUrl;

    private String publisher;

    private Integer minPlayers;

    private Integer maxPlayers;

    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer stock;

    /** Age minimum conseille. */
    private Integer minAge;

    /** Date de sortie : dans le futur = precommande, recente = nouveaute (voir GameController). */
    private LocalDate releaseDate;

    /** Pourcentage de remise en cours (1-100), null si pas de promotion active. */
    private Integer discountPercent;

    /** Fin de la promotion ; au-dela, discountPercent est ignore meme s'il est encore renseigne. */
    private Instant discountEndsAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "game_tags",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
