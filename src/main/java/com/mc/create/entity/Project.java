package com.mc.create.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目表，对应前端按功能类型独立存储的 mc_projects_*。
 * type 字段承担“功能类型”这一维度，如：皮肤/地图/模组等。
 */
@Data
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private String projectName;

    /** MC版本，如 1.20.1 */
    private String version;

    /** 状态：draft 草稿 / published 作品 */
    private String status;

    /** 展示图(base64 dataURL) */
    private String cover;

    /** 皮肤数据(base64)，供导入编辑器继续创作 */
    private String skinData;

    private LocalDateTime createTime;
}
