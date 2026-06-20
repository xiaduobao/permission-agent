package com.permission.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserSaveRequest {
    private Long id;
    private Long deptId;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String position;
    private Integer status;
    private List<Long> roleIds;
}
