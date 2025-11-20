package org.alexmond.sample.formauth.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final List<String> applications;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String username, String password, List<String> applications) {
        this.username = username;
        this.password = password;
        this.applications = applications != null ? applications : new ArrayList<>();
        this.authorities = new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return true;
    }
}
