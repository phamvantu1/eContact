package com.ec.contract.filter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final Integer id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Integer id, String username,
                             Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
    }

    public Integer getId() {
        return id;
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return null; } // JWT không dùng password
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
