package com.nookit.config;

import com.nookit.common.api.Result;
import com.nookit.common.api.ResultCode;
import com.nookit.common.constant.SecurityConstants;
import com.nookit.common.exception.AuthException;
import com.nookit.common.util.JsonUtil;
import com.nookit.common.util.JwtUtil;
import com.nookit.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 解析 {@code Authorization: Bearer xxx} 请求头，将登录态写入 {@link SecurityContextHolder}。
 * <p>
 * 异常路径直接写出 401 响应，避免被 Spring Security 默认页面截胡。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.AUTH_HEADER);
        String token = jwtUtil.resolveBearer(header);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            UserPrincipal principal = buildPrincipal(claims);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (AuthException ex) {
            writeError(response, ex.getCode(), ex.getMessage());
            return;
        } catch (Exception ex) {
            log.warn("jwt filter error: {}", ex.getMessage());
            writeError(response, ResultCode.TOKEN_INVALID.getCode(), ResultCode.TOKEN_INVALID.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private UserPrincipal buildPrincipal(Claims claims) {
        Object userIdClaim = claims.get(SecurityConstants.CLAIM_USER_ID);
        Long userId = userIdClaim == null ? null : Long.valueOf(userIdClaim.toString());
        String username = claims.get(SecurityConstants.CLAIM_USERNAME, String.class);
        String userType = claims.get(SecurityConstants.CLAIM_USER_TYPE, String.class);

        Set<String> roles = toSet(claims.get(SecurityConstants.CLAIM_ROLES));
        Set<String> permissions = toSet(claims.get(SecurityConstants.CLAIM_PERMISSIONS));

        return UserPrincipal.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .roles(roles)
                .permissions(permissions)
                .enabled(true)
                .accountNonLocked(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Set<String> toSet(Object claim) {
        if (claim instanceof List<?> list) {
            return Set.copyOf((List<String>) list);
        }
        return Collections.emptySet();
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(Result.error(code, message)));
    }
}
