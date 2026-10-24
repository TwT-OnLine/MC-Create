package com.mc.create.controller;

import com.mc.create.common.Result;
import com.mc.create.dto.UpdateAvatarDTO;
import com.mc.create.dto.UpdateProfileDTO;
import com.mc.create.entity.User;
import com.mc.create.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 当前登录用户信息（含 profile，不含密码） */
    @GetMapping("/current")
    public Result<User> current(@RequestAttribute("userId") Long userId) {
        return Result.ok(userService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestAttribute("userId") Long userId,
                                      @RequestBody UpdateProfileDTO dto) {
        return Result.ok(userService.updateProfile(userId, dto));
    }

    @PutMapping("/avatar")
    public Result<User> updateAvatar(@RequestAttribute("userId") Long userId,
                                     @RequestBody UpdateAvatarDTO dto) {
        return Result.ok(userService.updateAvatar(userId, dto.getAvatar()));
    }
}
