package cn.iocoder.yudao.module.ai1.framework.ai.core.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import cn.iocoder.yudao.module.ai1.enums.skill.Ai1SkillFileTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillFileService;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.agent.tools.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI1 SKILL 工具工厂（spring-ai-agent-utils SkillsTool）
 *
 * 把 Agent 绑定的 SKILL（数据库中的文件树）物化为本地技能目录，再构建 SkillsTool 与执行类工具：
 * <pre>
 * ${yudao.ai1.skill.root}/
 * └── {tenantId}/
 *     └── agent_{agentId}/          # Agent 级沙箱
 *         ├── .fingerprint          # 指纹标记，仅用于本地缓存校验
 *         └── {skillName}/          # 每个 SKILL 一个子目录
 *             ├── SKILL.md
 *             ├── scripts/
 *             └── reference/
 * </pre>
 * 变更检测：SKILL 内容变更会刷新其 update_time，这里按「编号:更新时间」计算指纹，不一致时整目录重建
 * （先清空再物化，杜绝换绑、删除后的残留文件）
 *
 * 注意：执行类工具（Shell、FileSystem 等）会在服务器上执行命令，与源工程行为一致，多租户不可信部署需自行评估
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1SkillToolFactory {

    // TODO @AI：是不是风格对齐噢？
    // TODO @AI：枚举类？
    /**
     * SKILL 入口文件名
     */
    private static final String FILE_NAME_SKILL = "SKILL.md";
    /**
     * 指纹标记文件名
     */
    // TODO @AI：枚举类？
    private static final String FILE_NAME_FINGERPRINT = ".fingerprint";

    @Resource
    private YudaoAi1Properties ai1Properties;

    @Resource
    private Ai1SkillService skillService;
    @Resource
    private Ai1SkillFileService skillFileService;

    // TODO @AI：guava？
    /**
     * Agent 物化快照缓存：Agent 编号 → 快照
     */
    private final Map<Long, SkillSnapshot> snapshotCache = new ConcurrentHashMap<>();
    /**
     * 按 Agent 串行物化的锁，避免并发重建互相破坏目录
     */
    private final Map<Long, Object> syncLocks = new ConcurrentHashMap<>();

    // TODO @AI：有点长，是不是分成 1. 2. 或者 1.1 1.2 这种？
    /**
     * 构建 Agent 绑定 SKILL 所需的全部工具：SkillsTool（注入 SKILL.md 内容）+ 执行类工具（限定于 Agent 物化根目录）
     *
     * @param agent Agent
     * @return 工具列表；未绑定有效 SKILL 时返回空列表
     */
    public List<Object> buildTools(Ai1AgentDO agent) {
        SkillSnapshot snapshot = sync(agent);
        if (snapshot == null) {
            return Collections.emptyList();
        }
        List<Object> tools = new ArrayList<>();
        // SkillsTool 要求至少一个有效 SKILL.md，否则只物化文件，不注册技能工具
        if (snapshot.skillsTool() != null) {
            tools.add(snapshot.skillsTool());
        }
        // 执行类工具：ShellTools（bash）、FileSystemTools（Read/Write/Edit）、Glob、Grep、ListDirectory
        Path root = snapshot.root();
        tools.add(ShellTools.builder().workingDirectory(root).build());
        tools.add(FileSystemTools.builder().allowedDirectory(root).build());
        tools.add(GlobTool.builder().workingDirectory(root).allowedDirectory(root).build());
        tools.add(GrepTool.builder().workingDirectory(root).allowedDirectory(root).build());
        tools.add(ListDirectoryTool.builder().workingDirectory(root).allowedDirectory(root).build());
        return tools;
    }

    /**
     * 清理 Agent 物化沙箱：摘除快照缓存并删除本地目录（Agent 删除时调用）
     *
     * @param tenantId 租户编号
     * @param agentId  Agent 编号
     */
    public void evict(Long tenantId, Long agentId) {
        snapshotCache.remove(agentId);
        try {
            FileUtil.del(buildAgentRoot(tenantId, agentId));
        } catch (Exception e) {
            log.warn("[evict][Agent({}) SKILL 沙箱清理失败]", agentId, e);
        }
    }

    /**
     * 同步并返回 Agent 物化快照：指纹变化则整目录重建，否则复用缓存
     */
    // TODO @AI：方法内注释，更好理解点。1. 2. 2.1 2.2 这种？毕竟有点长了；
    private SkillSnapshot sync(Ai1AgentDO agent) {
        // 1. 计算当前指纹
        if (CollUtil.isEmpty(agent.getSkillIds())) {
            return null;
        }
        List<Ai1SkillDO> skills = skillService.getSkillList(agent.getSkillIds());
        if (CollUtil.isEmpty(skills)) {
            return null;
        }
        // TODO @AI：fingerprint 需要抽独立方法么？
        String fingerprint = skills.stream().sorted(Comparator.comparing(Ai1SkillDO::getId))
                .map(skill -> skill.getId() + ":" + skill.getUpdateTime())
                .collect(Collectors.joining(";"));
        SkillSnapshot cached = snapshotCache.get(agent.getId());
        if (isSnapshotValid(cached, fingerprint)) {
            return cached;
        }

        // 2. 指纹变化，整目录重建（双重检查，并发请求下避免重复重建）
        synchronized (syncLocks.computeIfAbsent(agent.getId(), key -> new Object())) {
            cached = snapshotCache.get(agent.getId());
            if (isSnapshotValid(cached, fingerprint)) {
                return cached;
            }
            Path root = buildAgentRoot(agent.getTenantId(), agent.getId());
            try {
                FileUtil.del(root);
                Files.createDirectories(root);
                boolean hasSkill = false;
                for (Ai1SkillDO skill : skills) {
                    if (CommonStatusEnum.isEnable(skill.getStatus())
                            && materializeSkill(root.resolve(sanitize(skill.getName())), skill.getId())) {
                        hasSkill = true;
                    }
                }
                Files.writeString(root.resolve(FILE_NAME_FINGERPRINT), fingerprint, StandardCharsets.UTF_8);
                ToolCallback skillsTool = hasSkill ? SkillsTool.builder().addSkillsDirectory(root.toString()).build() : null;
                SkillSnapshot snapshot = new SkillSnapshot(fingerprint, root, skillsTool);
                snapshotCache.put(agent.getId(), snapshot);
                log.info("[sync][Agent({}) SKILL 物化完成，目录({})]", agent.getId(), root);
                return snapshot;
            } catch (Exception e) {
                log.warn("[sync][Agent({}) SKILL 物化失败]", agent.getId(), e);
                return cached;
            }
        }
    }

    // TODO @AI：方法内注释，更好理解点。1. 2. 2.1 2.2 这种？毕竟有点长了；
    /**
     * 本地快照是否有效：指纹一致，且标记文件存在、内容一致（防止目录被外部清理）
     */
    private static boolean isSnapshotValid(SkillSnapshot cached, String fingerprint) {
        // TODO @AI：notequals；
        if (cached == null || !fingerprint.equals(cached.fingerprint())) {
            return false;
        }
        try {
            Path marker = cached.root().resolve(FILE_NAME_FINGERPRINT);
            return Files.exists(marker) && fingerprint.equals(Files.readString(marker, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return false;
        }
    }

    // TODO @AI：方法内注释，更好理解点。1. 2. 2.1 2.2 这种？毕竟有点长了；
    /**
     * 物化单个 SKILL 的文件树到技能目录
     *
     * @return 是否包含 SKILL.md
     */
    private boolean materializeSkill(Path skillDirectory, Long skillId) throws IOException {
        // TODO @AI：拿到后，就是 map；
        List<Ai1SkillFileDO> files = skillFileService.getSkillFileListBySkillId(skillId);
        if (CollUtil.isEmpty(files)) {
            log.warn("[materializeSkill][SKILL({}) 无内容文件]", skillId);
            return false;
        }
        Map<Long, Ai1SkillFileDO> fileMap = convertMap(files, Ai1SkillFileDO::getId);
        Map<Long, Path> pathCache = new HashMap<>();
        boolean hasSkillFile = false;
        for (Ai1SkillFileDO file : files) {
            Path target = skillDirectory.resolve(resolveRelativePath(file, fileMap, pathCache));
            if (Ai1SkillFileTypeEnum.isFile(file.getType())) {
                hasSkillFile |= FILE_NAME_SKILL.equals(file.getName()) && Objects.equals(file.getParentId(), Ai1SkillFileDO.PARENT_ID_ROOT);
                Files.createDirectories(target.getParent());
                Files.writeString(target, StrUtil.nullToEmpty(file.getContent()), StandardCharsets.UTF_8);
            } else {
                Files.createDirectories(target);
            }
        }
        if (!hasSkillFile) {
            log.warn("[materializeSkill][SKILL({}) 缺少根级 SKILL.md]", skillId);
        }
        return hasSkillFile;
    }

    /**
     * 解析节点相对路径：沿 parentId 链自底向上拼接，每段名称净化以防目录穿越
     */
    private static Path resolveRelativePath(Ai1SkillFileDO file, Map<Long, Ai1SkillFileDO> fileMap, Map<Long, Path> pathCache) {
        Path cached = pathCache.get(file.getId());
        if (cached != null) {
            return cached;
        }
        Path segment = Path.of(sanitize(file.getName()));
        Ai1SkillFileDO parent = fileMap.get(file.getParentId());
        // 父节点为自身时（异常数据）按根级处理，避免无限递归
        Path path = parent == null || Objects.equals(parent.getId(), file.getId())
                ? segment : resolveRelativePath(parent, fileMap, pathCache).resolve(segment);
        pathCache.put(file.getId(), path);
        return path;
    }

    private Path buildAgentRoot(Long tenantId, Long agentId) {
        return Path.of(ai1Properties.getSkill().getRoot(), String.valueOf(tenantId), "agent_" + agentId);
    }

    // TODO @AI：通过 hutool 等，可以简化这些逻辑么？
    /**
     * 路径净化：屏蔽路径分隔符与穿越片段
     */
    private static String sanitize(String name) {
        if (StrUtil.isBlank(name)) {
            return "_";
        }
        return name.replace('\\', '_').replace('/', '_').replace("..", "_");
    }

    // TODO @AI：使用 lombok 替代掉，record 噢。
    /**
     * Agent 物化快照
     *
     * @param fingerprint 指纹
     * @param root        Agent 物化根目录
     * @param skillsTool  技能工具；无有效 SKILL.md 时为空
     */
    private record SkillSnapshot(String fingerprint, Path root, ToolCallback skillsTool) {
    }

}
