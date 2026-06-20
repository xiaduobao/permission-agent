package com.permission.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_ai_session")
public class SysAiSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Long userId;
    private String role;
    private String content;
    private String toolName;
    private String toolCallId;
    private String toolCalls;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
