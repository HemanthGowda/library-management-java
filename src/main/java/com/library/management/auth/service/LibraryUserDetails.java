package com.library.management.auth.service;

import com.library.management.user.model.Role;
import com.library.management.user.model.UserDocument;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class LibraryUserDetails implements UserDetails {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final Set<Role> roles;

    public LibraryUserDetails(UserDocument user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.isEnabled();
        this.roles = user.getRoles();
    }

    public LibraryUserDetails(String id, String email, Set<Role> roles, boolean enabled) {
        this.id = id;
        this.email = email;
        this.passwordHash = "";
        this.enabled = enabled;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
