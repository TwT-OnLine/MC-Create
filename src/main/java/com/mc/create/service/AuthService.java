package com.mc.create.service;

import com.mc.create.dto.LoginDTO;
import com.mc.create.dto.RegisterDTO;

import java.util.Map;

/**
 * 认证服务接口：注册 / 登录 / 退出。
 * 由 impl/AuthServiceImpl 实现。
 */
public interface AuthService {

    /** 注册：校验 → BCrypt 加密 → 入库，返回 {userId, username} */
    Map<String, Object> register(RegisterDTO dto);

    /** 登录：校验密码 → 签发 token，返回 {token, userId, username} */
    Map<String, Object> login(LoginDTO dto);

    /** 退出：吊销 token */
    void logout(String token);
}
