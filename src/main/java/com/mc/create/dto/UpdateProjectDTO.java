package com.mc.create.dto;

import lombok.Data;

/**
 * 更新项目 DTO：改名 / 展示图 / 发布状态 / 皮肤数据。
 * 仅更新传入的非空字段。
 */
@Data
public class UpdateProjectDTO {
    private String name;
    private String cover;
    private String status;
    private String skinData;
}
