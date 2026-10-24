package com.mc.create.service;

import com.mc.create.dto.SaveVanillaEditDTO;
import com.mc.create.dto.VanillaEntryVO;

import java.util.List;
import java.util.Map;

/**
 * 原版生物/方块/物品修改器：目录查询、修改保存、模组导出。
 */
public interface VanillaService {

    /** 目录：某版本某分类的全部条目（附带“已修改”标记） */
    List<VanillaEntryVO> catalog(Long userId, Long projectId, String version, String kind);

    /** 条目详情：原版属性 + 该项目的修改（含贴图 dataURL） */
    Map<String, Object> detail(Long userId, Long projectId, String version, String kind, String entryId);

    /** 该项目所有修改的摘要（不含贴图大字段），用于目录页角标与计数 */
    List<VanillaEntryVO> edits(Long userId, Long projectId);

    /** 保存（或覆盖）一条修改 */
    void saveEdit(Long userId, SaveVanillaEditDTO dto);

    /** 恢复原版：删除该条目的修改记录 */
    void resetEdit(Long userId, Long projectId, String kind, String entryId);

    /**
     * 导出可拖入 mods 的 Forge 模组 jar
     *
     * @param outName 长度 ≥1 的数组，回填实际使用的文件名
     * @param modName 用户在导出弹窗里填的模组名；为空则退回作品名
     */
    byte[] exportMod(Long userId, Long projectId, String[] outName, String modName);
}
