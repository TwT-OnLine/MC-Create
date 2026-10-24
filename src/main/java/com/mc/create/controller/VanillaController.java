package com.mc.create.controller;

import com.mc.create.common.Result;
import com.mc.create.dto.SaveVanillaEditDTO;
import com.mc.create.dto.VanillaEntryVO;
import com.mc.create.service.VanillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 原版生物/方块/物品修改器接口。
 * 所有接口都需要登录态（拦截器注入 userId）。
 */
@RestController
@RequestMapping("/api/vanilla")
@RequiredArgsConstructor
public class VanillaController {

    private final VanillaService vanillaService;

    /** 目录：某分类全部条目（含是否已修改） */
    @GetMapping("/catalog")
    public Result<List<VanillaEntryVO>> catalog(@RequestAttribute("userId") Long userId,
                                                @RequestParam(required = false) Long projectId,
                                                @RequestParam(defaultValue = "1.20.1") String version,
                                                @RequestParam(defaultValue = "block") String kind) {
        return Result.ok(vanillaService.catalog(userId, projectId, version, kind));
    }

    /** 条目详情：原版属性 + 本项目修改 */
    @GetMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestAttribute("userId") Long userId,
                                              @RequestParam(required = false) Long projectId,
                                              @RequestParam(defaultValue = "1.20.1") String version,
                                              @RequestParam String kind,
                                              @RequestParam String entryId) {
        return Result.ok(vanillaService.detail(userId, projectId, version, kind, entryId));
    }

    /** 本项目所有已修改条目（轻量，不含贴图） */
    @GetMapping("/edits")
    public Result<List<VanillaEntryVO>> edits(@RequestAttribute("userId") Long userId,
                                              @RequestParam Long projectId) {
        return Result.ok(vanillaService.edits(userId, projectId));
    }

    /** 保存一条修改 */
    @PostMapping("/save")
    public Result<Void> save(@RequestAttribute("userId") Long userId,
                             @RequestBody SaveVanillaEditDTO dto) {
        vanillaService.saveEdit(userId, dto);
        return Result.ok();
    }

    /** 恢复原版 */
    @PostMapping("/reset")
    public Result<Void> reset(@RequestAttribute("userId") Long userId,
                              @RequestParam Long projectId,
                              @RequestParam String kind,
                              @RequestParam String entryId) {
        vanillaService.resetEdit(userId, projectId, kind, entryId);
        return Result.ok();
    }

    /** 导出可拖入 mods 的 Forge 模组；name 为空时用作品名当文件名 */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestAttribute("userId") Long userId,
                                         @RequestParam Long projectId,
                                         @RequestParam(required = false) String name) {
        String[] out = new String[1];
        byte[] data = vanillaService.exportMod(userId, projectId, out, name);
        String encoded = URLEncoder.encode(out[0], StandardCharsets.UTF_8).replace("+", "%20");
        // filename 只放 ASCII（中文会被换成下划线）当兜底，真名走 RFC 5987 的 filename*；
        // 中文直接写进 filename= 会让 Tomcat 整条丢掉这个响应头
        String ascii = out[0].replaceAll("[^\\x20-\\x7E]", "_").replace("\"", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }
}
