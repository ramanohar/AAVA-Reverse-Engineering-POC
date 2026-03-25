package com.collaberadigital.cove.security;



import com.collaberadigital.cove.model.entity.UserEntity;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//@Setter
@NoArgsConstructor
@Configuration
public class CurrentUser implements UserDetails {

    private String email;
    private String password;
    private Boolean active;
    private List<GrantedAuthority> authorities;

    public CurrentUser(UserEntity client){
        this.email = client.getEmail();
        this.password = client.getPassword();
        this.active = client.getIsActive();
        this.authorities = List.of(client.getRole().split(","))
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
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
        return active;
    }
}
