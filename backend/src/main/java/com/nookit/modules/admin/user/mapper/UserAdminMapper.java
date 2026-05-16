package com.nookit.modules.admin.user.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.org.User;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.user.dto.AdminUserVO;
import com.nookit.modules.admin.user.dto.StudentUserVO;
import com.nookit.modules.admin.user.dto.UserPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAdminMapper extends BaseMapperX<User> {

    IPage<StudentUserVO> pageStudents(Page<StudentUserVO> page, @Param("q") UserPageQuery query);

    StudentUserVO findStudentDetail(@Param("id") Long id);

    IPage<AdminUserVO> pageAdmins(Page<AdminUserVO> page, @Param("q") UserPageQuery query);
}
