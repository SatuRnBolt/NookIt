package com.nookit.modules.auth.service;

import com.nookit.modules.auth.dto.LoginRequest;
import com.nookit.modules.auth.dto.LoginResponse;
import com.nookit.modules.auth.dto.UserInfoVO;
import com.nookit.security.UserPrincipal;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserInfoVO me(UserPrincipal principal);
}
