package com.bgs.boardgameshop.security;

import com.bgs.boardgameshop.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun compte pour l'email " + email));
    }
}
