package com.authsystem.authsession.auth.service;

import com.authsystem.authsession.auth.security.principal.PrincipalDetails;
import com.authsystem.authsession.common.exception.CustomException;
import com.authsystem.authsession.common.exception.ErrorType;
import com.authsystem.authsession.user.entity.User;
import com.authsystem.authsession.user.repository.UserRepository;
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
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));

        return new PrincipalDetails(user);
    }
}
