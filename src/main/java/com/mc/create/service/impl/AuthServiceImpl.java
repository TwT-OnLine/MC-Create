package com.mc.create.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.create.common.BizException;
import com.mc.create.config.JwtUtil;
import com.mc.create.dto.LoginDTO;
import com.mc.create.dto.RegisterDTO;
import com.mc.create.entity.User;
import com.mc.create.mapper.UserMapper;
import com.mc.create.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> register(RegisterDTO dto) {
        String username = dto.getUsername() == null ? null : dto.getUsername().trim();
        String password = dto.getPassword();

        if (username == null || username.isBlank())
            throw new BizException("用户名和密码不能为空");
        if (username.length() < 3 || username.length() > 20)
            throw new BizException("用户名需 3~20 位");
        if (password == null || password.length() < 8 || password.length() > 18)
            throw new BizException("密码需 8~18 位");
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*"))
            throw new BizException("密码必须同时包含字母和数字");
        if (!password.equals(dto.getConfirmPassword()))
            throw new BizException("两次输入的密码不一致");

        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exists != null && exists > 0)
            throw new BizException("用户名已存在");

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));   // 只存 BCrypt 哈希，不存明文
        user.setNickname(username);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        String username = dto.getUsername() == null ? null : dto.getUsername().trim();
        String password = dto.getPassword();
        // 空用户名/密码先给业务提示：否则 BCrypt.matches(null, ...) 会抛
        // IllegalArgumentException，被全局异常处理器当成 500 返回
        if (username == null || username.isEmpty() || password == null || password.isEmpty())
            throw new BizException("用户名和密码不能为空");

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null)
            throw new BizException("登录失败：账号不存在");
        if (!encoder.matches(password, user.getPassword()))
            throw new BizException("登录失败：密码错误");

        String token = jwtUtil.createToken(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    @Override
    public void logout(String token) {
        // JWT 是无状态的，服务端不保存会话，无法真正吊销；
        // 退出登录靠前端删除本地 token 实现。若需强制失效，可接入黑名单/Redis。
    }
}
