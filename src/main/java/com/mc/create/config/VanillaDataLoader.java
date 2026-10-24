package com.mc.create.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动时初始化原版数据：
 * 1) 建表（vanilla_entry / vanilla_edit，均 IF NOT EXISTS）
 * 2) 把 jar 内 vanilla/vanilla-1.20.1.json 导入 vanilla_entry（按 version+kind 判断，已导入则跳过）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VanillaDataLoader implements ApplicationRunner {

    private static final String[] JSON_KEYS = {"blocks", "items", "mobs"};
    private static final String[] KINDS = {"block", "item", "mob"};

    private static final String DDL_ENTRY = "CREATE TABLE IF NOT EXISTS vanilla_entry (" +
            "id BIGINT NOT NULL AUTO_INCREMENT," +
            "version VARCHAR(16) NOT NULL," +
            "kind VARCHAR(16) NOT NULL," +
            "entry_id VARCHAR(64) NOT NULL," +
            "display_name VARCHAR(128) DEFAULT NULL," +
            "en_name VARCHAR(160) DEFAULT NULL," +
            "tex VARCHAR(255) DEFAULT NULL," +
            "props LONGTEXT," +
            "PRIMARY KEY (id)," +
            "UNIQUE KEY uk_entry (version, kind, entry_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static final String DDL_EDIT = "CREATE TABLE IF NOT EXISTS vanilla_edit (" +
            "id BIGINT NOT NULL AUTO_INCREMENT," +
            "project_id BIGINT NOT NULL," +
            "user_id BIGINT NOT NULL," +
            "version VARCHAR(16) NOT NULL," +
            "kind VARCHAR(16) NOT NULL," +
            "entry_id VARCHAR(64) NOT NULL," +
            "props LONGTEXT," +
            "texture LONGTEXT," +
            "model LONGTEXT," +
            "update_time DATETIME DEFAULT NULL," +
            "PRIMARY KEY (id)," +
            "UNIQUE KEY uk_edit (project_id, kind, entry_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            init();
        } catch (Exception e) {
            log.error("[vanilla] 原版数据初始化失败：{}", e.getMessage(), e);
        }
    }

    private void init() throws Exception {
        jdbc.execute(DDL_ENTRY);
        jdbc.execute(DDL_EDIT);
        // 老库升级：vanilla_edit 以前没有 model 列（导入 3D 模型用）
        try {
            jdbc.execute("ALTER TABLE vanilla_edit ADD COLUMN model LONGTEXT");
            log.info("[vanilla] vanilla_edit 已新增 model 列");
        } catch (Exception ignore) {
            // 列已存在，忽略
        }

        ClassPathResource res = new ClassPathResource("vanilla/vanilla-1.20.1.json");
        if (!res.exists()) {
            log.warn("[vanilla] 缺少资源 vanilla/vanilla-1.20.1.json，跳过导入");
            return;
        }

        JsonNode root;
        try (var in = res.getInputStream()) {
            root = objectMapper.readTree(in);
        }
        String version = root.path("version").asText("1.20.1");

        for (int i = 0; i < KINDS.length; i++) {
            String kind = KINDS[i];
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM vanilla_entry WHERE version = ? AND kind = ?",
                    Integer.class, version, kind);
            if (exists != null && exists > 0) {
                log.info("[vanilla] {} / {} 已存在 {} 条，跳过导入", version, kind, exists);
                continue;
            }
            JsonNode arr = root.path(JSON_KEYS[i]);
            List<Object[]> rows = new ArrayList<>(arr.size());
            for (JsonNode e : arr) {
                rows.add(new Object[]{
                        version,
                        kind,
                        e.path("id").asText(),
                        e.path("name").asText(),
                        e.path("en").asText(),
                        e.path("tex").asText(),
                        e.path("props").toString()
                });
            }
            if (rows.isEmpty()) {
                continue;
            }
            jdbc.batchUpdate(
                    "INSERT IGNORE INTO vanilla_entry(version, kind, entry_id, display_name, en_name, tex, props) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)", rows);
            log.info("[vanilla] 导入 {} / {} 共 {} 条", version, kind, rows.size());
        }
    }
}
