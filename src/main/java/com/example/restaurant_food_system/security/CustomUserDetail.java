package com.example.restaurant_food_system.security;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter @Setter
@Builder
public class CustomUserDetail implements UserDetails {
    private String userId;
    private String fullName;
    private String email;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetail(String userId, String fullName, String email, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.authorities = authorities;
    }


    public String getId() {
        return this.userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return this.fullName;
    }

}
