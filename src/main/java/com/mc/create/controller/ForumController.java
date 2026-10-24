package com.mc.create.controller;

import com.mc.create.common.Result;
import com.mc.create.dto.CreateMessageDTO;
import com.mc.create.dto.ForumMessageVO;
import com.mc.create.service.ForumService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛·留言墙接口。路径受 AuthInterceptor 保护，发布/查看都需登录。
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    /** 全部留言（按时间倒序，含作者昵称/头像） */
    @GetMapping("/messages")
    public Result<List<ForumMessageVO>> listMessages() {
        return Result.ok(forumService.listMessages());
    }

    /** 发布留言 */
    @PostMapping("/messages")
    public Result<ForumMessageVO> createMessage(@RequestAttribute("userId") Long userId,
                                                @RequestBody CreateMessageDTO dto) {
        return Result.ok(forumService.createMessage(userId, dto));
    }
}
