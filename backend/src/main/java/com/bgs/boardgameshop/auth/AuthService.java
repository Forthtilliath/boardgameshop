package com.bgs.boardgameshop.auth;

import com.bgs.boardgameshop.security.JwtService;
import com.bgs.boardgameshop.security.SecurityUser;
import com.bgs.boardgameshop.user.EmailAlreadyUsedException;
import com.bgs.boardgameshop.user.Role;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import com.bgs.boardgameshop.user.dto.AuthResponse;
import com.bgs.boardgameshop.user.dto.LoginRequest;
import com.bgs.boardgameshop.user.dto.RegisterRequest;
import com.bgs.boardgameshop.user.dto.UserResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Verification manuelle des identifiants (pas d'AuthenticationManager complet) :
 * moins de boilerplate a comprendre pour un simple login email/mot de passe.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
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

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(new SecurityUser(user));
        return new AuthResponse(token, UserResponse.fromEntity(user));
    }
}
