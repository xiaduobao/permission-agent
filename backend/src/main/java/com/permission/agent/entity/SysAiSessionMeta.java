package com.permission.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_ai_session_meta")
public class SysAiSessionMeta {
    @TableId
    private String sessionId;
    private Long userId;
    private Long tenantId;
    private String title;
    private Integer messageCount;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
