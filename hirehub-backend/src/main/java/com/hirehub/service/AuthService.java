package com.hirehub.service;

import com.hirehub.dto.auth.AuthResponse;
import com.hirehub.dto.auth.ChangePasswordRequest;
import com.hirehub.dto.auth.LoginRequest;
import com.hirehub.dto.auth.RefreshTokenRequest;
import com.hirehub.dto.auth.RegisterRequest;
import com.hirehub.entity.ApplicantProfile;
import com.hirehub.entity.RecruiterProfile;
import com.hirehub.entity.User;
import com.hirehub.enums.RoleName;
import com.hirehub.exception.DuplicateResourceException;
import com.hirehub.exception.UnauthorizedException;
import com.hirehub.repository.UserRepository;
import com.hirehub.security.CurrentUser;
import com.hirehub.security.JwtService;
import com.hirehub.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        attachProfile(user, request.role());
        User savedUser = userRepository.save(user);
        log.info("Registration completed for email {}", savedUser.getEmail());
        return toAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        log.info("Login completed for email {}", user.getEmail());
        return toAuthResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UserPrincipal principal = CurrentUser.get();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.revokeAllForUser(user.getId());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = refreshTokenService.rotate(request.refreshToken());
        return toAuthResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private void attachProfile(User user, RoleName role) {
        if (role == RoleName.ROLE_APPLICANT) {
            ApplicantProfile profile = new ApplicantProfile();
            profile.setUser(user);
            user.setApplicantProfile(profile);
        }
        if (role == RoleName.ROLE_RECRUITER) {
            RecruiterProfile profile = new RecruiterProfile();
            profile.setUser(user);
            user.setRecruiterProfile(profile);
        }
    }

    private AuthResponse toAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer", accessTokenExpirationMs, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
