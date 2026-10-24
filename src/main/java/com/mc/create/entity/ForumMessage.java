package com.mc.create.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言墙留言表 forum_message。
 * 作者信息（昵称/头像）不冗余存储，读取时联查 user 表，
 * 这样用户改昵称/头像后留言自动跟随，避免快照过期。
 */
@Data
@TableName("forum_message")
public class ForumMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 留言人用户ID */
    private Long userId;

    /** 留言内容（最多 200 字） */
    private String content;

    private LocalDateTime createTime;
}
