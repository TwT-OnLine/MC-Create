package com.mc.create.dto;

import lombok.Data;

/**
 * 发布留言请求体：仅 content。
 * 非空与 200 字上限由 service 校验（与项目内其他模块一致的风格）。
 */
@Data
public class CreateMessageDTO {
    private String content;
}
