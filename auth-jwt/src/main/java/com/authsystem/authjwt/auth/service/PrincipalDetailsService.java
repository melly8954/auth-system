package com.authsystem.authjwt.auth.service;

import com.authsystem.authjwt.auth.security.principal.PrincipalDetails;
import com.authsystem.authjwt.common.exception.CustomException;
import com.authsystem.authjwt.common.exception.ErrorType;
import com.authsystem.authjwt.user.entity.User;
import com.authsystem.authjwt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        return new PrincipalDetails(user);
    }
}
