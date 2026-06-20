package com.permission.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.agent.entity.SysUser;
import com.permission.agent.entity.SysRole;
import com.permission.agent.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = 1 " +
            "ORDER BY m.sort")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT a.permission FROM sys_api a " +
            "INNER JOIN sys_role_api ra ON a.id = ra.api_id " +
            "INNER JOIN sys_user_role ur ON ra.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND a.deleted = 0 AND a.status = 1")
    List<String> selectApiPermissionsByUserId(@Param("userId") Long userId);
}
