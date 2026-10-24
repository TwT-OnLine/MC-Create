package com.mc.create.service.impl;

import com.mc.create.common.BizException;
import com.mc.create.dto.UpdateProfileDTO;
import com.mc.create.entity.User;
import com.mc.create.mapper.UserMapper;
import com.mc.create.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = getCurrentUser(userId);
        if (user == null) throw new BizException("用户不存在");

        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            if (dto.getNickname().trim().length() > 20)
                throw new BizException("昵称需 1~20 个字符");
            user.setNickname(dto.getNickname().trim());
        }
        if (dto.getEmail() != null) user.setEmail(dto.getEmail().trim());
        if (dto.getBio() != null) user.setBio(dto.getBio().trim());

        userMapper.updateById(user);
        return user;
    }

    @Override
    public User updateAvatar(Long userId, String avatar) {
        User user = getCurrentUser(userId);
        if (user == null) throw new BizException("用户不存在");
        user.setAvatar(avatar);
        userMapper.updateById(user);
        return user;
    }
}
