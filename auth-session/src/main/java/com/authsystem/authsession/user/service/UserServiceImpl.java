package com.authsystem.authsession.user.service;

import com.authsystem.authsession.common.exception.CustomException;
import com.authsystem.authsession.common.exception.ErrorType;
import com.authsystem.authsession.user.dto.SignUpRequest;
import com.authsystem.authsession.user.dto.SignUpResponse;
import com.authsystem.authsession.user.entity.User;
import com.authsystem.authsession.user.entity.UserProfile;
import com.authsystem.authsession.user.entity.UserRole;
import com.authsystem.authsession.user.entity.UserStatus;
import com.authsystem.authsession.user.repository.UserProfileRepository;
import com.authsystem.authsession.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorType.DUPLICATE_USERNAME);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorType.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .name(request.getName())
                .nickname(request.getNickname())
                .build();
        userProfileRepository.save(profile);

        return SignUpResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(profile.getName())
                .nickname(profile.getNickname())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
