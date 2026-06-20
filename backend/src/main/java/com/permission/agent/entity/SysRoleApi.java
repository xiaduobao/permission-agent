package com.permission.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_role_api")
public class SysRoleApi {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long apiId;
}
