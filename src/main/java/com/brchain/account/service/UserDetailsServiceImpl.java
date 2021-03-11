package com.brchain.account.service;

import com.brchain.account.entity.UserEntity;
import com.brchain.account.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) {
        Optional<UserEntity> userOptional = userRepository.findByUserId(userId);
        UserEntity user = userOptional
                .orElseThrow(() -> new EntityNotFoundException("No user Found with userId: " + userId));

        return new org.springframework.security
                .core.userdetails.User(user.getUserId(), user.getUserPassword(),
                user.isActive(), true, true,
                true, getAuthorities("USER"));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}