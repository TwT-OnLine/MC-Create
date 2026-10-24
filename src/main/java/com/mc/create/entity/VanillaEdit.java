package com.mc.create.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 原版修改记录：某个项目对某个原版条目做过的属性/贴图改动。
 * 以 (project_id, kind, entry_id) 唯一，重复保存即覆盖。
 */
@Data
@TableName("vanilla_edit")
public class VanillaEdit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long userId;

    private String version;

    /** block / item / mob */
    private String kind;

    private String entryId;

    /** 修改后的属性 JSON（未修改的字段沿用原版值） */
    private String props;

    /** 修改后的贴图，dataURL(image/png) */
    private String texture;

    /**
     * 导入的 3D 模型（JSON 字符串 {tw,th,name,cubes:[...]}），
     * 来自基岩版 *.geo.json 或 Java 版 models/entity/*.json。
     * null = 用原版几何。
     */
    private String model;

    private LocalDateTime updateTime;
}
