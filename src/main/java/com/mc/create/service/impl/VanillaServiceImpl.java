package com.mc.create.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mc.create.common.BizException;
import com.mc.create.dto.SaveVanillaEditDTO;
import com.mc.create.dto.VanillaEntryVO;
import com.mc.create.entity.Project;
import com.mc.create.entity.VanillaEdit;
import com.mc.create.entity.VanillaEntry;
import com.mc.create.mapper.ProjectMapper;
import com.mc.create.mapper.VanillaEditMapper;
import com.mc.create.mapper.VanillaEntryMapper;
import com.mc.create.service.VanillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class VanillaServiceImpl implements VanillaService {

    private static final String TEMPLATE = "mod-template/mccreate-vanilla.jar";
    private static final String OVERRIDES_ENTRY = "data/mccreate/vanilla_overrides.json";
    private static final String TEX_PREFIX = "assets/minecraft/textures/";
    private static final String MODEL_PREFIX = "assets/minecraft/models/mccreate/";
    /** 1×1 占位图标（64 字节的合法 PNG），避免资源包面板报警告 */
    private static final byte[] PACK_ICON = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
    /** pack.mcmeta 同时声明成模组资源包，自定义模型才能被原版模型加载器读到 */
    private static final String PACK_META = """
            {
              "pack": {
                "pack_format": 15,
                "description": "MC Create - 原版重制"
              },
              "mods": {
                "mccreate_vanilla": "MC Create - 原版重制"
              }
            }
            """;

    private final VanillaEntryMapper entryMapper;
    private final VanillaEditMapper editMapper;
    private final ProjectMapper projectMapper;
    private final ObjectMapper objectMapper;
    private final com.mc.create.service.VanillaTexStore texStore;

    /* ------------------------------------------------------------------ 目录 */

    @Override
    public List<VanillaEntryVO> catalog(Long userId, Long projectId, String version, String kind) {
        String ver = version == null || version.isBlank() ? "1.20.1" : version;
        List<VanillaEntry> rows = entryMapper.selectList(new LambdaQueryWrapper<VanillaEntry>()
                .eq(VanillaEntry::getVersion, ver)
                .eq(VanillaEntry::getKind, kind)
                .orderByAsc(VanillaEntry::getEntryId));

        Set<String> edited = new HashSet<>();
        Set<String> texEdited = new HashSet<>();
        if (projectId != null) {
            for (VanillaEdit e : editsOf(projectId)) {
                String key = e.getKind() + ":" + e.getEntryId();
                edited.add(key);
                if (e.getTexture() != null && !e.getTexture().isEmpty()) {
                    texEdited.add(key);
                }
            }
        }

        List<VanillaEntryVO> out = new ArrayList<>(rows.size());
        for (VanillaEntry e : rows) {
            VanillaEntryVO vo = new VanillaEntryVO();
            vo.setKind(e.getKind());
            vo.setEntryId(e.getEntryId());
            vo.setName(e.getDisplayName());
            vo.setEn(e.getEnName());
            vo.setTex(e.getTex());
            vo.setProps(e.getProps());
            String key = e.getKind() + ":" + e.getEntryId();
            vo.setEdited(edited.contains(key));
            vo.setTexEdited(texEdited.contains(key));
            out.add(vo);
        }
        return out;
    }

    @Override
    public Map<String, Object> detail(Long userId, Long projectId, String version, String kind, String entryId) {
        String ver = version == null || version.isBlank() ? "1.20.1" : version;
        VanillaEntry entry = entryMapper.selectOne(new LambdaQueryWrapper<VanillaEntry>()
                .eq(VanillaEntry::getVersion, ver)
                .eq(VanillaEntry::getKind, kind)
                .eq(VanillaEntry::getEntryId, entryId)
                .last("LIMIT 1"));
        if (entry == null) {
            throw new BizException("条目不存在：" + kind + " / " + entryId);
        }

        VanillaEdit edit = null;
        if (projectId != null) {
            edit = editMapper.selectOne(new LambdaQueryWrapper<VanillaEdit>()
                    .eq(VanillaEdit::getProjectId, projectId)
                    .eq(VanillaEdit::getKind, kind)
                    .eq(VanillaEdit::getEntryId, entryId)
                    .last("LIMIT 1"));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", entry.getKind());
        out.put("entryId", entry.getEntryId());
        out.put("name", entry.getDisplayName());
        out.put("en", entry.getEnName());
        out.put("tex", entry.getTex());
        out.put("baseProps", entry.getProps());
        out.put("props", edit != null && edit.getProps() != null ? edit.getProps() : entry.getProps());
        out.put("texture", edit != null ? edit.getTexture() : null);
        out.put("model", edit != null ? edit.getModel() : null);
        out.put("texData", texStore.dataUrl(entry.getTex()));
        out.put("edited", edit != null);
        return out;
    }

    @Override
    public List<VanillaEntryVO> edits(Long userId, Long projectId) {
        List<VanillaEntryVO> out = new ArrayList<>();
        for (VanillaEdit e : editsOf(projectId)) {
            VanillaEntryVO vo = new VanillaEntryVO();
            vo.setKind(e.getKind());
            vo.setEntryId(e.getEntryId());
            vo.setEdited(true);
            vo.setTexEdited(e.getTexture() != null && !e.getTexture().isEmpty());
            out.add(vo);
        }
        return out;
    }

    /* ------------------------------------------------------------------ 保存 */

    @Override
    public void saveEdit(Long userId, SaveVanillaEditDTO dto) {
        if (dto.getProjectId() == null) {
            throw new BizException("缺少项目 ID，请从项目里进入修改器");
        }
        Project project = requireOwned(userId, dto.getProjectId());
        String version = dto.getVersion() == null || dto.getVersion().isBlank()
                ? (project.getVersion() == null ? "1.20.1" : project.getVersion())
                : dto.getVersion();

        VanillaEdit old = editMapper.selectOne(new LambdaQueryWrapper<VanillaEdit>()
                .eq(VanillaEdit::getProjectId, dto.getProjectId())
                .eq(VanillaEdit::getKind, dto.getKind())
                .eq(VanillaEdit::getEntryId, dto.getEntryId())
                .last("LIMIT 1"));

        String texture;
        if (dto.getTexture() == null) {
            // 不改贴图：沿用已有
            texture = old == null ? null : old.getTexture();
        } else if (dto.getTexture().isEmpty()) {
            // 恢复原版贴图
            texture = null;
        } else {
            texture = dto.getTexture();
        }

        String model;
        if (dto.getModel() == null) {
            // 不改模型：沿用已有
            model = old == null ? null : old.getModel();
        } else if (dto.getModel().isEmpty()) {
            // 恢复原版模型
            model = null;
        } else {
            model = dto.getModel();
        }

        VanillaEdit edit = old == null ? new VanillaEdit() : old;
        edit.setProjectId(dto.getProjectId());
        edit.setUserId(userId);
        edit.setVersion(version);
        edit.setKind(dto.getKind());
        edit.setEntryId(dto.getEntryId());
        edit.setProps(dto.getProps());
        edit.setTexture(texture);
        edit.setModel(model);
        edit.setUpdateTime(LocalDateTime.now());

        if (old == null) {
            editMapper.insert(edit);
        } else {
            editMapper.updateById(edit);
        }
    }

    @Override
    public void resetEdit(Long userId, Long projectId, String kind, String entryId) {
        requireOwned(userId, projectId);
        editMapper.delete(new LambdaQueryWrapper<VanillaEdit>()
                .eq(VanillaEdit::getProjectId, projectId)
                .eq(VanillaEdit::getKind, kind)
                .eq(VanillaEdit::getEntryId, entryId));
    }

    /* ------------------------------------------------------------------ 导出 */

    @Override
    public byte[] exportMod(Long userId, Long projectId, String[] outName, String modName) {
        Project project = requireOwned(userId, projectId);
        List<VanillaEdit> list = editsOf(projectId);
        if (list.isEmpty()) {
            throw new BizException("这个项目还没有修改任何原版内容，先改几个方块/物品/生物再来导出吧");
        }

        String version = project.getVersion() == null || project.getVersion().isBlank()
                ? "1.20.1" : project.getVersion();

        // 贴图路径表
        Map<String, String> texOf = new HashMap<>();
        for (VanillaEntry e : entryMapper.selectList(new LambdaQueryWrapper<VanillaEntry>()
                .eq(VanillaEntry::getVersion, version))) {
            texOf.put(e.getKind() + ":" + e.getEntryId(), e.getTex());
        }

        // 导出弹窗填的模组名优先；没填就用作品名兜底。
        // 这个名字同时决定 jar 文件名、包名和 README，保持三处一致。
        String chosen = (modName == null || modName.isBlank())
                ? project.getProjectName() : modName.trim();
        String safe = safeName(chosen);

        // 覆盖数据
        ObjectNode root = objectMapper.createObjectNode();
        root.put("packName", safe);
        root.put("version", version);
        ObjectNode blocks = root.putObject("blocks");
        ObjectNode items = root.putObject("items");
        ObjectNode mobs = root.putObject("mobs");
        // key = "kind:entryId"，值 = 导入的 3D 几何（tw/th/cubes）
        ObjectNode models = root.putObject("models");

        Map<String, byte[]> textures = new LinkedHashMap<>();
        // 导入的 3D 模型 → 自定义资源包模型（assets/minecraft/models/mccreate/*.json），
        // 配合 overrides.json 里的 models 段，由 Forge 端在运行时重新定义模型的贴图槽位。
        Map<String, byte[]> modelFiles = new LinkedHashMap<>();
        int imported = 0;
        for (VanillaEdit e : list) {
            ObjectNode props = null;
            if (e.getProps() != null && !e.getProps().isBlank()) {
                try {
                    JsonNode node = objectMapper.readTree(e.getProps());
                    if (node != null && node.isObject()) {
                        props = (ObjectNode) node;
                    }
                } catch (Exception ignored) {
                    // 坏数据不影响导出
                }
            }
            if (props != null) {
                switch (e.getKind()) {
                    case "block" -> blocks.set(e.getEntryId(), props);
                    case "item" -> items.set(e.getEntryId(), props);
                    case "mob" -> mobs.set(e.getEntryId(), props);
                    default -> {
                    }
                }
            }
            String tex = e.getTexture();
            if (tex != null && tex.startsWith("data:image")) {
                byte[] png = decodeDataUrl(tex);
                String path = texOf.get(e.getKind() + ":" + e.getEntryId());
                if (png != null && path != null) {
                    textures.put(TEX_PREFIX + path, png);
                }
            }
            // 导入的 3D 模型：原样带到 overrides.json，由 Forge 端重建几何
            String model = e.getModel();
            if (model != null && !model.isBlank()) {
                try {
                    JsonNode node = objectMapper.readTree(model);
                    if (node != null && node.isObject()) {
                        models.set(e.getKind() + ":" + e.getEntryId(), node);
                        imported++;
                        String key = e.getKind() + ":" + e.getEntryId();
                        byte[] packModel = packModel(e.getEntryId(), node, texOf.get(key));
                        if (packModel != null) {
                            modelFiles.put(MODEL_PREFIX + safeName(e.getEntryId()) + ".json", packModel);
                        }
                    }
                } catch (Exception ignored) {
                    // 坏数据不影响导出
                }
            }
        }

        byte[] template = readTemplate();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(template.length + 256 * 1024);
        Set<String> written = new HashSet<>();
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(template));
             ZipOutputStream zout = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {

            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()
                        || name.startsWith("data/mccreate/")
                        || name.startsWith(TEX_PREFIX)
                        || name.equals("pack.mcmeta")
                        || name.equals("MCCreate-README.txt")) {
                    continue;
                }
                if (!written.add(name)) {
                    continue;
                }
                zout.putNextEntry(new ZipEntry(name));
                zin.transferTo(zout);
                zout.closeEntry();
            }

            put(zout, OVERRIDES_ENTRY, objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(root), written);
            for (Map.Entry<String, byte[]> t : textures.entrySet()) {
                put(zout, t.getKey(), t.getValue(), written);
            }
            for (Map.Entry<String, byte[]> m : modelFiles.entrySet()) {
                put(zout, m.getKey(), m.getValue(), written);
            }
            put(zout, "pack.mcmeta", PACK_META.getBytes(StandardCharsets.UTF_8), written);
            put(zout, "mccreate_vanilla.png", PACK_ICON, written);
            put(zout, "MCCreate-README.txt",
                    readme(safe, project, version, list.size(), textures.size(), imported), written);
        } catch (BizException be) {
            throw be;
        } catch (Exception ex) {
            throw new BizException("打包模组失败：" + ex.getMessage());
        }

        if (outName != null && outName.length > 0) {
            outName[0] = safe + "-1.20.1.jar";
        }
        return bos.toByteArray();
    }

    /* ------------------------------------------------------------------ 工具 */

    private List<VanillaEdit> editsOf(Long projectId) {
        if (projectId == null) {
            return List.of();
        }
        return editMapper.selectList(new LambdaQueryWrapper<VanillaEdit>()
                .eq(VanillaEdit::getProjectId, projectId)
                .orderByAsc(VanillaEdit::getKind)
                .orderByAsc(VanillaEdit::getEntryId));
    }

    private Project requireOwned(Long userId, Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在");
        }
        if (!project.getUserId().equals(userId)) {
            throw new BizException("无权操作该项目");
        }
        return project;
    }

    private byte[] readTemplate() {
        ClassPathResource res = new ClassPathResource(TEMPLATE);
        if (!res.exists()) {
            throw new BizException("服务端缺少模组模板（mod-template/mccreate-vanilla.jar），请联系管理员");
        }
        try (InputStream in = res.getInputStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new BizException("读取模组模板失败：" + e.getMessage());
        }
    }

    /* ---------------------------------------------- 导入模型 → 资源包模型 */

    /**
     * 把编辑器里的 3D 几何导出成一个原版资源包模型 json，让基岩版几何（0..16 立方
     * 单位、原点在脚底中心）能在 Java 版里以立方体盒子呈现。
     *
     * <p>坐标约定与原版实体模型一致：y 朝上，z 朝南，x 朝东。
     */
    private byte[] packModel(String entryId, JsonNode geo, String texPath) {
        JsonNode cubes = geo.get("cubes");
        if (cubes == null || !cubes.isArray() || cubes.isEmpty()) {
            return null;
        }
        String tex = texPath == null || texPath.isBlank() ? "block/stone" : texPath;
        // 库里存的是带扩展名的贴图路径（entity/creeper/creeper.png），
        // 但模型 json 的 textures 引用必须是不带扩展名的资源路径
        if (tex.endsWith(".png")) {
            tex = tex.substring(0, tex.length() - 4);
        }

        ObjectNode model = objectMapper.createObjectNode();
        model.put("credit", "MC Create");
        model.put("ambientocclusion", false);
        ObjectNode texs = model.putObject("textures");
        texs.put("particle", tex);
        texs.put("skin", tex);

        var elements = model.putArray("elements");
        for (JsonNode cube : cubes) {
            elements.add(element(cube, tex));
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(model);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 一个盒子 → 一个 element。
     *
     * <p>编辑器存储的 cube 结构（见 js/model-import.js）：
     * {@code { o:[x,y,z], ex:[..], ey:[..], ez:[..], u, v, w, h, d, mir, f? }}，
     * o 是盒子最小角，ex/ey/ez 是三条棱向量（已含骨骼旋转），w/h/d 是未旋转尺寸，
     * f 是可选的逐面 uv 覆盖。这里把棱向量正交化回轴对齐的包围盒——资源包模型
     * 的 element 只能轴对齐，旋转过的盒子会退化成它的外接盒。
     */
    private ObjectNode element(JsonNode c, String tex) {
        double[] vx = vec(c.get("ex"), 1, 0, 0);
        double[] vy = vec(c.get("ey"), 0, 1, 0);
        double[] vz = vec(c.get("ez"), 0, 0, 1);
        double[] o = vec(c.get("o"), 0, 0, 0);

        // 盒子 8 个角点 → 轴对齐包围盒
        double x1 = Double.MAX_VALUE, y1 = Double.MAX_VALUE, z1 = Double.MAX_VALUE;
        double x2 = -Double.MAX_VALUE, y2 = -Double.MAX_VALUE, z2 = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double sx = (i & 1) == 0 ? 0 : 1;
            double sy = (i & 2) == 0 ? 0 : 1;
            double sz = (i & 4) == 0 ? 0 : 1;
            double px = o[0] + sx * vx[0] + sy * vy[0] + sz * vz[0];
            double py = o[1] + sx * vx[1] + sy * vy[1] + sz * vz[1];
            double pz = o[2] + sx * vx[2] + sy * vy[2] + sz * vz[2];
            x1 = Math.min(x1, px);
            y1 = Math.min(y1, py);
            z1 = Math.min(z1, pz);
            x2 = Math.max(x2, px);
            y2 = Math.max(y2, py);
            z2 = Math.max(z2, pz);
        }
        if (x2 - x1 < 0.01) x2 = x1 + 0.01;
        if (y2 - y1 < 0.01) y2 = y1 + 0.01;
        if (z2 - z1 < 0.01) z2 = z1 + 0.01;

        ObjectNode el = objectMapper.createObjectNode();
        var from = el.putArray("from");
        from.add(round(x1));
        from.add(round(y1));
        from.add(round(z1));
        var to = el.putArray("to");
        to.add(round(x2));
        to.add(round(y2));
        to.add(round(z2));

        // UV：基岩版的 uv 是「盒子展开图」左上角，逐面各占一块
        double u = c.path("u").asDouble(0);
        double v = c.path("v").asDouble(0);
        double w = Math.abs(c.path("w").asDouble(x2 - x1));
        double h = Math.abs(c.path("h").asDouble(y2 - y1));
        double d = Math.abs(c.path("d").asDouble(z2 - z1));
        JsonNode f = c.get("f");

        ObjectNode faces = el.putObject("faces");
        // 逐面 uv 顺序：左下、右下、右上、左上
        // 北/南面：展开图里宽 w、高 h，位于 (u, v)
        // 东/西面：宽 d，位于 x 方向偏移 w 之后
        // 上/下面：位于 y 方向偏移 h 之后，宽 w 高 d
        face(faces, "north", u + w, v + h, u, v + h, u, v, u + w, v);
        face(faces, "south", u, v + h, u + w, v + h, u + w, v, u, v);
        face(faces, "east", u + w + d, v + h, u + w, v + h, u + w, v, u + w + d, v);
        face(faces, "west", u + w, v + h, u + w + d, v + h, u + w + d, v, u + w, v);
        face(faces, "up", u + w, v + d, u, v + d, u, v, u + w, v);
        face(faces, "down", u, v + d, u + w, v + d, u + w, v, u, v);
        return el;
    }

    private static double[] vec(JsonNode node, double dx, double dy, double dz) {
        if (node == null || !node.isArray() || node.size() < 3) {
            return new double[] { dx, dy, dz };
        }
        return new double[] { node.get(0).asDouble(dx), node.get(1).asDouble(dy),
                node.get(2).asDouble(dz) };
    }

    private void face(ObjectNode faces, String dir,
                      double a, double b, double c2, double d2, double e, double f,
                      double g, double h) {
        ObjectNode node = faces.putObject(dir);
        node.put("texture", "#skin");
        var uv = node.putArray("uv");
        uv.add(round(a));
        uv.add(round(b));
        uv.add(round(c2));
        uv.add(round(d2));
        uv.add(round(e));
        uv.add(round(f));
        uv.add(round(g));
        uv.add(round(h));
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private static void put(ZipOutputStream zout, String name, byte[] data, Set<String> written) throws Exception {
        if (!written.add(name)) {
            return;
        }
        zout.putNextEntry(new ZipEntry(name));
        zout.write(data);
        zout.closeEntry();
    }

    private static byte[] decodeDataUrl(String dataUrl) {
        try {
            int comma = dataUrl.indexOf(',');
            if (comma < 0) {
                return null;
            }
            return Base64.getDecoder().decode(dataUrl.substring(comma + 1));
        } catch (Exception e) {
            return null;
        }
    }

    private static String safeName(String name) {
        String n = name == null || name.isBlank() ? "MC Create 作品" : name.trim();
        n = n.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        if (n.length() > 40) {
            n = n.substring(0, 40);
        }
        return n;
    }

    private static byte[] readme(String modName, Project project, String version,
                                 int entries, int textures, int models) {
        String text = """
                MC Create 导出的原版重制模组
                ==============================
                模组名：%s
                来源作品：%s
                版本：%s（Forge）
                改动条目：%d 个（其中替换贴图 %d 张、导入 3D 模型 %d 个）

                安装方式
                ------------------------------
                1. 安装 Minecraft %s + Forge %s
                2. 把本 jar 直接拖进 .minecraft/mods 文件夹
                3. 启动游戏即可生效

                生效内容
                ------------------------------
                · 方块硬度（挖掘速度按比例缩放，0 = 秒破）
                · 物品攻击伤害 / 攻击速度 / 护甲值 / 护甲韧性（物品 tooltip 同步变化）
                · 生物最大生命 / 攻击力 / 护甲 / 移动速度 / 掉落经验
                · 所有替换过的贴图（模组内置资源，进游戏直接生效）
                · 导入的 3D 模型：按基岩版几何还原成方块状盒子的自定义模型

                仅记录、未在运行时生效的内容
                ------------------------------
                爆炸抗性、发光等级、采集工具与等级、堆叠数量、耐久、
                食物营养与饱和度、怪物阵营等属于注册期数据，模组运行时
                无法安全修改，编辑器中仅作记录保存。

                说明：原版生物的骨架由 Java 版硬编码的 ModelPart 构建，运行时
                无法整体替换成自定义几何；导入生物模型会以自定义资源包模型的
                方式生效，用于方块/物品外观。生物贴图替换不受影响。
                """.formatted(
                modName,
                project.getProjectName() == null ? "未命名" : project.getProjectName(),
                version, entries, textures, models, version, "47.x");
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
