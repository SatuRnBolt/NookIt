package com.nookit.modules.admin.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.org.AuthIdentity;
import com.nookit.common.domain.org.User;
import com.nookit.common.domain.rbac.UserRole;
import com.nookit.common.exception.BusinessException;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.user.dto.*;
import com.nookit.modules.admin.user.mapper.AuthIdentityMapper;
import com.nookit.modules.admin.user.mapper.UserAdminMapper;
import com.nookit.modules.admin.user.mapper.UserRoleMapper;
import com.nookit.modules.admin.user.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserAdminMapper userAdminMapper;
    private final UserRoleMapper userRoleMapper;
    private final AuthIdentityMapper authIdentityMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<StudentUserVO> pageStudents(UserPageQuery query) {
        Page<StudentUserVO> pg = new Page<>(query.getPage(), query.getPageSize());
        userAdminMapper.pageStudents(pg, query);
        return PageResult.from(pg);
    }

    @Override
    public StudentUserVO getStudentDetail(Long id) {
        StudentUserVO vo = userAdminMapper.findStudentDetail(id);
        if (vo == null) throw new ResourceNotFoundException(ResultCode.USER_NOT_FOUND);
        return vo;
    }

    @Override
    public void updateStudentStatus(Long id, String status) {
        User u = userAdminMapper.selectById(id);
        if (u == null) throw new ResourceNotFoundException(ResultCode.USER_NOT_FOUND);
        User upd = new User();
        upd.setId(id);
        upd.setAccountStatus(status);
        userAdminMapper.updateById(upd);
    }

    @Override
    public PageResult<AdminUserVO> pageAdmins(UserPageQuery query) {
        Page<AdminUserVO> pg = new Page<>(query.getPage(), query.getPageSize());
        userAdminMapper.pageAdmins(pg, query);
        return PageResult.from(pg);
    }

    @Override
    @Transactional
    public void createAdmin(AdminCreateReq req) {
        // Check email uniqueness
        Long count = userAdminMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (count > 0) throw new BusinessException(ResultCode.EMAIL_DUPLICATED);

        User user = new User();
        user.setFullName(req.getName());
        user.setEmail(req.getEmail());
        user.setUserType("staff");
        user.setAccountStatus("active");
        user.setMustChangePassword(true);
        userAdminMapper.insert(user);

        AuthIdentity identity = new AuthIdentity();
        identity.setUserId(user.getId());
        identity.setIdentityType("email_password");
        identity.setIdentityKey(req.getEmail());
        identity.setAuthSource("local");
        identity.setCredentialHash(passwordEncoder.encode(req.getPassword()));
        identity.setIsVerified(true);
        identity.setVerifiedAt(LocalDateTime.now());
        authIdentityMapper.insert(identity);

        UserRole ur = new UserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(req.getRoleId());
        ur.setScopeType("global");
        ur.setIsPrimary(true);
        ur.setEffectiveFrom(LocalDateTime.now());
        userRoleMapper.insert(ur);
    }

    @Override
    public void updateAdmin(Long id, AdminCreateReq req) {
        User u = userAdminMapper.selectById(id);
        if (u == null) throw new ResourceNotFoundException(ResultCode.USER_NOT_FOUND);
        u.setFullName(req.getName());
        u.setEmail(req.getEmail());
        userAdminMapper.updateById(u);
    }

    @Override
    public void assignRole(Long id, Long roleId) {
        if (userAdminMapper.selectById(id) == null) throw new ResourceNotFoundException(ResultCode.USER_NOT_FOUND);
        // Delete existing primary role then insert new
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, id).eq(UserRole::getIsPrimary, true));
        UserRole ur = new UserRole();
        ur.setUserId(id);
        ur.setRoleId(roleId);
        ur.setScopeType("global");
        ur.setIsPrimary(true);
        ur.setEffectiveFrom(LocalDateTime.now());
        userRoleMapper.insert(ur);
    }

    @Override
    public void deleteAdmin(Long id) {
        if (userAdminMapper.selectById(id) == null) throw new ResourceNotFoundException(ResultCode.USER_NOT_FOUND);
        userAdminMapper.deleteById(id);
    }
}
