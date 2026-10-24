package com.mc.create.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 原版贴图仓库。
 * <p>
 * 前端是 file:// 直接打开的，把 <img src="assets/..."> 画进 canvas 会污染画布
 * （toDataURL 抛 SecurityError），所以贴图必须由后端以 data: URL 交给页面，
 * 这样 canvas 才干净、能直接改像素。
 * <p>
 * 贴图打包在 classpath 的 vanilla-tex.zip 里，第一次用到时整体读进内存（约 1.9MB）。
 */
@Slf4j
@Component
public class VanillaTexStore {

    private static final String ZIP = "vanilla-tex.zip";

    private volatile Map<String, byte[]> cache;

    private Map<String, byte[]> load() {
        Map<String, byte[]> map = cache;
        if (map != null) {
            return map;
        }
        synchronized (this) {
            if (cache != null) {
                return cache;
            }
            Map<String, byte[]> tmp = new HashMap<>(4096);
            try (InputStream in = new ClassPathResource(ZIP).getInputStream();
                 ZipInputStream zis = new ZipInputStream(in)) {
                ZipEntry e;
                byte[] buf = new byte[8192];
                while ((e = zis.getNextEntry()) != null) {
                    if (e.isDirectory()) {
                        continue;
                    }
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    int n;
                    while ((n = zis.read(buf)) > 0) {
                        bos.write(buf, 0, n);
                    }
                    tmp.put(e.getName(), bos.toByteArray());
                }
            } catch (Exception ex) {
                log.warn("[vanilla] 贴图包读取失败: {}", ex.getMessage());
            }
            log.info("[vanilla] 贴图包载入 {} 张", tmp.size());
            cache = tmp;
            return tmp;
        }
    }

    /** @param tex 形如 block/stone.png（相对 assets/minecraft/textures） */
    public byte[] bytes(String tex) {
        if (tex == null || tex.isBlank()) {
            return null;
        }
        return load().get(tex.replace('\\', '/'));
    }

    /** 给前端用的 data: URL；找不到就返回 null */
    public String dataUrl(String tex) {
        byte[] b = bytes(tex);
        if (b == null) {
            return null;
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(b);
    }
}
