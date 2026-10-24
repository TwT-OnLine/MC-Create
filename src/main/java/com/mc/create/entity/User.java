package com.mc.create.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表，对应前端 localStorage 里的 mc_users。
 * password 存 BCrypt 哈希，绝不返回给前端（@JsonIgnore）。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希，仅内部使用，序列化时忽略 */
    @JsonIgnore
    private String password;

    private String nickname;

    private String email;

    private String bio;

    private String avatar;          // base64 dataURL

    private LocalDateTime createTime;
}
