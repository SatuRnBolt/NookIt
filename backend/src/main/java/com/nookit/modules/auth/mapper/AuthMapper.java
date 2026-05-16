package com.nookit.modules.auth.mapper;

import com.nookit.modules.auth.dto.UserInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AuthMapper {

    /** 根据登录账号（邮箱/学号）查询用户凭据 */
    Map<String, Object> findCredentialByIdentity(@Param("identityKey") String identityKey);

    /** 查询用户角色列表 */
    List<String> findRolesByUserId(@Param("userId") Long userId);

    /** 查询用户权限列表 */
    List<String> findPermissionsByUserId(@Param("userId") Long userId);

    /** 更新最后登录时间 */
    void updateLastLoginAt(@Param("userId") Long userId);
}
