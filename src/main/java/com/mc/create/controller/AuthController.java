package com.mc.create.controller;

import com.mc.create.common.Result;
import com.mc.create.dto.LoginDTO;
import com.mc.create.dto.RegisterDTO;
import com.mc.create.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth,
                               @RequestHeader(value = "token", required = false) String token) {
        String t = auth != null && auth.startsWith("Bearer ") ? auth.substring(7) : token;
        authService.logout(t);
        return Result.ok();
    }
}
