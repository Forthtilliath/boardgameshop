package com.bgs.boardgameshop.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByShareToken(String shareToken);

    /** Pour le graphe "inscriptions par jour" du dashboard admin. */
    List<User> findByCreatedAtAfter(Instant since);
}
