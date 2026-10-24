package com.mc.create.dto;

import lombok.Data;

/**
 * 保存一条原版条目的修改（属性 + 可选贴图 + 可选导入模型）。
 * texture 传 null 表示“贴图不变”；传空串表示“恢复原版贴图”。
 * model 传 null 表示“模型不变”；传空串表示“恢复原版模型”。
 */
@Data
public class SaveVanillaEditDTO {

    private Long projectId;

    /** block / item / mob */
    private String kind;

    private String entryId;

    private String version;

    /** 修改后的属性 JSON 字符串 */
    private String props;

    /** dataURL(image/png) */
    private String texture;

    /** 导入的 3D 模型 JSON：{tw,th,name,cubes:[{o,ex,ey,ez,u,v,w,h,d,mir}]} */
    private String model;
}
