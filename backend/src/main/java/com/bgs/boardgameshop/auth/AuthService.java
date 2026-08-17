package com.bgs.boardgameshop.auth;

import com.bgs.boardgameshop.user.EmailAlreadyUsedException;
import com.bgs.boardgameshop.user.Role;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import com.bgs.boardgameshop.user.dto.LoginRequest;
import com.bgs.boardgameshop.user.dto.RegisterRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Vérification manuelle des identifiants (pas d'AuthenticationManager complet) :
 * moins de boilerplate à comprendre pour un simple login email/mot de passe.
 *
 * <p>Ne s'occupe que de vérifier/créer le compte et renvoie l'entité {@link User} :
 * l'émission des cookies (access + refresh) est du ressort du contrôleur, via
 * {@code AuthCookieService}, pas de ce service.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        return user;
    }
}
