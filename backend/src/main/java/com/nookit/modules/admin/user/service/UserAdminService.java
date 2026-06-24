package com.nookit.modules.admin.user.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.user.dto.*;

public interface UserAdminService {

    // Students
    PageResult<StudentUserVO> pageStudents(UserPageQuery query);

    StudentUserVO getStudentDetail(Long id);

    void updateStudentStatus(Long id, String status);

    // Admins
    PageResult<AdminUserVO> pageAdmins(UserPageQuery query);

    void createAdmin(AdminCreateReq req);

    void updateAdmin(Long id, AdminCreateReq req);

    void assignRole(Long id, Long roleId);

    void deleteAdmin(Long id);
}
