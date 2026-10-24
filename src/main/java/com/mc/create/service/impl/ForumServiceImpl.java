package com.mc.create.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.create.common.BizException;
import com.mc.create.dto.CreateMessageDTO;
import com.mc.create.dto.ForumMessageVO;
import com.mc.create.entity.ForumMessage;
import com.mc.create.entity.User;
import com.mc.create.mapper.ForumMessageMapper;
import com.mc.create.mapper.UserMapper;
import com.mc.create.service.ForumService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl implements ForumService {

    private final ForumMessageMapper messageMapper;
    private final UserMapper userMapper;

    public ForumServiceImpl(ForumMessageMapper messageMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<ForumMessageVO> listMessages() {
        List<ForumMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<ForumMessage>().orderByDesc(ForumMessage::getCreateTime));
        if (messages.isEmpty()) return Collections.emptyList();

        // 批量取留言人信息，组装成 作者ID -> User 的映射
        Set<Long> userIds = messages.stream()
                .map(ForumMessage::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return messages.stream().map(m -> {
            User u = userMap.get(m.getUserId());
            ForumMessageVO vo = new ForumMessageVO();
            vo.setId(m.getId());
            vo.setUserId(m.getUserId());
            vo.setNickname(u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "匿名");
            vo.setAvatar(u != null ? u.getAvatar() : "");
            vo.setContent(m.getContent());
            vo.setCreateTime(m.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ForumMessageVO createMessage(Long userId, CreateMessageDTO dto) {
        String content = dto.getContent();
        if (content == null || content.trim().isEmpty())
            throw new BizException("留言内容不能为空");
        content = content.trim();
        if (content.length() > 200)
            throw new BizException("留言最多 200 字");

        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException("用户不存在");

        ForumMessage msg = new ForumMessage();
        msg.setUserId(userId);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);

        ForumMessageVO vo = new ForumMessageVO();
        vo.setId(msg.getId());
        vo.setUserId(userId);
        vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setContent(content);
        vo.setCreateTime(msg.getCreateTime());
        return vo;
    }
}
