package com.permission.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_ai_pending_action")
public class SysAiPendingAction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Long userId;
    private String toolName;
    private String arguments;
    private String preview;
    /** 0待确认 1已执行 2已拒绝 3已过期 */
    private Integer status;
    private LocalDateTime expireTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
