package com.permission.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleSaveRequest {
    private Long id;
    private String name;
    private String code;
    private Integer dataScope;
    private String remark;
    private Integer status;
    private List<Long> menuIds;
    private List<Long> apiIds;
    private List<Long> deptIds;
}
