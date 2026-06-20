package com.permission.agent.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.permission.agent.entity.SysUser;
import com.permission.agent.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        List<String> permissions = userMapper.selectApiPermissionsByUserId(user.getId());
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        permissions.add("ROLE_USER");
        return new LoginUser(user, permissions);
    }
}
