package com.mc.create.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 原版条目表：1.20.1 全部方块/物品/生物的真实注册数据（只读目录，由启动时从 jar 资源导入）。
 * props 是 JSON 字符串，字段随 kind 不同而不同：
 *   block -> hardness/blast/luminous/tool/tier/transparent/diggable/stack/states
 *   item  -> max_stack/durability/damage/speed/nutrition/saturation/armor/toughness...
 *   mob   -> health/damage/armor/speed/xp/hostile/faction/width/height
 */
@Data
@TableName("vanilla_entry")
public class VanillaEntry {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** MC 版本，如 1.20.1 */
    private String version;

    /** block / item / mob */
    private String kind;

    /** 注册名，如 stone / diamond_sword / zombie */
    private String entryId;

    /** 官方中文名 */
    private String displayName;

    /** 英文名 */
    private String enName;

    /** 贴图相对路径，如 block/stone.png、entity/zombie/zombie.png */
    private String tex;

    /** 属性 JSON */
    private String props;
}
