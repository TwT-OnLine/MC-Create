package com.mc.create.service;

import com.mc.create.dto.UpdateProfileDTO;
import com.mc.create.entity.User;

/**
 * 用户资料服务接口。
 * 由 impl/UserServiceImpl 实现。
 */
public interface UserService {

    /** 当前登录用户（password 已 @JsonIgnore，不会返回） */
    User getCurrentUser(Long userId);

    /** 更新资料（昵称/邮箱/简介），返回更新后的用户 */
    User updateProfile(Long userId, UpdateProfileDTO dto);

    /** 更换头像：base64 dataURL，返回更新后的用户 */
    User updateAvatar(Long userId, String avatar);
}
