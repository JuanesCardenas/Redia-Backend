package com.redia.back.security;

import com.redia.back.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptador de la entidad User al modelo de seguridad de Spring Security.
 */
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public String getId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isActivo();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActivo();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isActivo();
    }

    @Override
    public boolean isEnabled() {
        return user.isActivo();
    }
}