package com.nookit.security;

import com.nookit.common.constant.SecurityConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Spring Security 的 {@link UserDetails} 实现，承载登录态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long userId;
    private String username;
    private String fullName;
    private String userType;
    @Builder.Default
    private Set<String> roles = Collections.emptySet();
    @Builder.Default
    private Set<String> permissions = Collections.emptySet();
    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    private boolean accountNonLocked = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        Stream.ofNullable(roles).flatMap(Collection::stream)
                .map(r -> new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + r))
                .forEach(authorities::add);
        Stream.ofNullable(permissions).flatMap(Collection::stream)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return authorities;
    }

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    @Override
    public String getPassword() {
        // JWT 模式下不需要保存密码
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
