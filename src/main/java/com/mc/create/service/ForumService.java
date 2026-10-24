package com.mc.create.service;

import com.mc.create.dto.CreateMessageDTO;
import com.mc.create.dto.ForumMessageVO;

import java.util.List;

/**
 * 论坛·留言墙服务接口。
 * 由 impl/ForumServiceImpl 实现。
 */
public interface ForumService {

    /** 全部留言（按时间倒序，含作者昵称/头像） */
    List<ForumMessageVO> listMessages();

    /** 发布留言，返回带作者信息的留言 */
    ForumMessageVO createMessage(Long userId, CreateMessageDTO dto);
}
