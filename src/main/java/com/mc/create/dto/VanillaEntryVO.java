package com.mc.create.dto;

import lombok.Data;

/**
 * 原版条目在目录页/详情页的返回结构（不含大字段 base64）。
 */
@Data
public class VanillaEntryVO {

    private String kind;
    private String entryId;
    private String name;
    private String en;
    private String tex;

    /** 原版属性 JSON 字符串（直接透传给前端解析） */
    private String props;

    /** 该项目是否已修改过（目录页显示角标） */
    private boolean edited;

    /** 该项目是否替换过贴图 */
    private boolean texEdited;
}
