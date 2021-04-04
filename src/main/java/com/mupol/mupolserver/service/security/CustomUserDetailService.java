package com.mupol.mupolserver.service.security;

import com.mupol.mupolserver.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String userPk) {
        return (UserDetails) userRepository.findById(Long.valueOf(userPk)).orElseThrow(() -> new IllegalArgumentException("no such user"));
    }
}
