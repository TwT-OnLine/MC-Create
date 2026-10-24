package com.mc.create.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言展示对象：附带上作者当前昵称与头像（联查 user 表得到）。
 */
@Data
public class ForumMessageVO {

    private Long id;

    private Long userId;

    /** 作者昵称（无则退回用户名） */
    private String nickname;

    /** 作者头像（base64 dataURL，可为空） */
    private String avatar;

    private String content;

    private LocalDateTime createTime;
}
