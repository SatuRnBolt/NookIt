package com.nookit.modules.auth.service.impl;

import com.nookit.common.api.ResultCode;
import com.nookit.common.constant.SecurityConstants;
import com.nookit.common.exception.AuthException;
import com.nookit.common.exception.BusinessException;
import com.nookit.common.util.JwtUtil;
import com.nookit.modules.auth.dto.LoginRequest;
import com.nookit.modules.auth.dto.LoginResponse;
import com.nookit.modules.auth.dto.UserInfoVO;
import com.nookit.modules.auth.mapper.AuthMapper;
import com.nookit.modules.auth.service.AuthService;
import com.nookit.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        Map<String, Object> credential = authMapper.findCredentialByIdentity(request.getIdentity());
        if (credential == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        String accountStatus = (String) credential.get("accountStatus");
        if (!"active".equals(accountStatus)) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        String credentialHash = (String) credential.get("credentialHash");
        if (credentialHash == null || !passwordEncoder.matches(request.getPassword(), credentialHash)) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        Long userId = ((Number) credential.get("userId")).longValue();
        String userType = (String) credential.get("userType");
        String fullName = (String) credential.get("fullName");
        String email = (String) credential.get("email");

        List<String> roles = authMapper.findRolesByUserId(userId);
        List<String> permissions = authMapper.findPermissionsByUserId(userId);

        authMapper.updateLastLoginAt(userId);

        String token = jwtUtil.generateAccessToken(userId, email != null ? email : String.valueOf(userId),
                Map.of(
                        SecurityConstants.CLAIM_USER_TYPE, userType,
                        SecurityConstants.CLAIM_ROLES, roles,
                        SecurityConstants.CLAIM_PERMISSIONS, permissions
                ));

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(userId);
        userInfo.setName(fullName);
        userInfo.setEmail(email);
        userInfo.setUserType(userType);
        userInfo.setRoles(roles);
        userInfo.setPermissions(permissions);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUser(userInfo);
        return resp;
    }

    @Override
    public UserInfoVO me(UserPrincipal principal) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(principal.getUserId());
        vo.setName(principal.getFullName());
        vo.setEmail(principal.getUsername());
        vo.setUserType(principal.getUserType());
        vo.setRoles(List.copyOf(principal.getRoles()));
        vo.setPermissions(List.copyOf(principal.getPermissions()));
        return vo;
    }
}
