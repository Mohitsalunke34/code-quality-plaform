package com.trident.codequality.service;

import com.trident.codequality.domain.Role;
import com.trident.codequality.domain.User;
import com.trident.codequality.dto.auth.AuthResponse;
import com.trident.codequality.dto.auth.LoginRequest;
import com.trident.codequality.dto.auth.RegisterRequest;
import com.trident.codequality.exception.DuplicateResourceException;
import com.trident.codequality.repository.UserRepository;
import com.trident.codequality.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(user);
        return issueTokens(request.username());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return issueTokens(request.username());
    }

    private AuthResponse issueTokens(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);
        return AuthResponse.bearer(access, refresh, jwtService.accessTokenExpiresInSeconds());
    }
}
