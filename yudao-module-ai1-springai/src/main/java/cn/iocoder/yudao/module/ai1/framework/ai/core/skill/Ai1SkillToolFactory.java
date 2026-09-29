package cn.iocoder.yudao.module.ai1.framework.ai.core.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import cn.iocoder.yudao.module.ai1.enums.skill.Ai1SkillFileTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillFileService;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.agent.tools.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.io.File;
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
 * └── agent_{agentId}/              # Agent 级沙箱（Agent 编号全局唯一，无需按租户分层）
 *     ├── .fingerprint              # 指纹标记，仅用于本地缓存校验
 *     └── {skillName}/              # 每个 SKILL 一个子目录
 *         ├── SKILL.md
 *         ├── scripts/
 *         └── reference/
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

    /**
     * 指纹标记文件名
     */
    private static final String FILE_NAME_FINGERPRINT = ".fingerprint";

    /**
     * 快照缓存的最大数量
     */
    private static final int SNAPSHOT_CACHE_MAX_SIZE = 1000;

    @Resource
    private YudaoAi1Properties ai1Properties;

    @Resource
    private Ai1SkillService skillService;
    @Resource
    private Ai1SkillFileService skillFileService;

    /**
     * Agent 物化快照缓存：Agent 编号 → 快照
     *
     * 集群下各节点独立缓存，每次使用前按「编号:更新时间」指纹校验，Agent 换绑或 SKILL 变更后自动重建，无需跨节点失效
     */
    private final Cache<Long, SkillSnapshot> snapshotCache = CacheBuilder.newBuilder()
            .maximumSize(SNAPSHOT_CACHE_MAX_SIZE).build();
    /**
     * 按 Agent 串行物化的锁，避免并发重建互相破坏目录
     */
    private final Map<Long, Object> syncLocks = new ConcurrentHashMap<>();

    /**
     * 构建 Agent 绑定 SKILL 所需的全部工具：SkillsTool（注入 SKILL.md 内容）+ 执行类工具（限定于 Agent 物化根目录）
     *
     * @param agent Agent
     * @return 工具列表；未绑定有效 SKILL 时返回空列表
     */
    public List<Object> buildTools(Ai1AgentDO agent) {
        // 1. 同步物化快照；未绑定有效 SKILL 时，不提供任何工具
        SkillSnapshot snapshot = sync(agent);
        if (snapshot == null) {
            return Collections.emptyList();
        }

        // 2. SkillsTool：要求至少一个有效 SKILL.md，否则只物化文件，不注册技能工具
        List<Object> tools = new ArrayList<>();
        if (snapshot.getSkillsTool() != null) {
            tools.add(snapshot.getSkillsTool());
        }

        // 3. 执行类工具：ShellTools（bash）、FileSystemTools（Read/Write/Edit）、Glob、Grep、ListDirectory
        Path root = snapshot.getRoot();
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
     * @param agentId Agent 编号
     */
    public void evict(Long agentId) {
        snapshotCache.invalidate(agentId);
        try {
            FileUtil.del(buildAgentRoot(agentId));
        } catch (Exception e) {
            log.warn("[evict][Agent({}) SKILL 沙箱清理失败]", agentId, e);
        }
    }

    /**
     * 同步并返回 Agent 物化快照：指纹变化则整目录重建，否则复用缓存
     */
    private SkillSnapshot sync(Ai1AgentDO agent) {
        // 1.1 查询绑定的 SKILL
        if (CollUtil.isEmpty(agent.getSkillIds())) {
            return null;
        }
        List<Ai1SkillDO> skills = skillService.getSkillList(agent.getSkillIds());
        if (CollUtil.isEmpty(skills)) {
            return null;
        }
        // 1.2 指纹一致时，直接复用缓存
        String fingerprint = buildFingerprint(skills);
        SkillSnapshot cached = snapshotCache.getIfPresent(agent.getId());
        if (isSnapshotValid(cached, fingerprint)) {
            return cached;
        }

        // 2. 指纹变化，整目录重建（双重检查，并发请求下避免重复重建）
        synchronized (syncLocks.computeIfAbsent(agent.getId(), key -> new Object())) {
            cached = snapshotCache.getIfPresent(agent.getId());
            if (isSnapshotValid(cached, fingerprint)) {
                return cached;
            }
            Path root = buildAgentRoot(agent.getId());
            try {
                // 2.1 清空目录后，逐个物化开启的 SKILL
                FileUtil.del(root);
                FileUtil.mkdir(root.toFile());
                boolean hasSkill = false;
                for (Ai1SkillDO skill : skills) {
                    if (CommonStatusEnum.isEnable(skill.getStatus())
                            && materializeSkill(root.resolve(sanitize(skill.getName())), skill.getId())) {
                        hasSkill = true;
                    }
                }
                // 2.2 写入指纹标记，构建技能工具并缓存快照
                FileUtil.writeUtf8String(fingerprint, root.resolve(FILE_NAME_FINGERPRINT).toFile());
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

    /**
     * 计算 SKILL 指纹：按编号排序后拼接「编号:更新时间」，任一 SKILL 换绑或内容变更都会导致指纹变化
     *
     * @param skills SKILL 列表
     * @return 指纹
     */
    private static String buildFingerprint(List<Ai1SkillDO> skills) {
        return skills.stream().sorted(Comparator.comparing(Ai1SkillDO::getId))
                .map(skill -> skill.getId() + ":" + skill.getUpdateTime())
                .collect(Collectors.joining(";"));
    }

    /**
     * 本地快照是否有效：指纹一致，且标记文件存在、内容一致（防止目录被外部清理）
     */
    private static boolean isSnapshotValid(SkillSnapshot cached, String fingerprint) {
        // 1. 内存快照不存在，或指纹不一致
        if (cached == null || ObjUtil.notEqual(fingerprint, cached.getFingerprint())) {
            return false;
        }
        // 2. 标记文件不存在或内容不一致，说明目录已被外部清理或篡改
        File marker = cached.getRoot().resolve(FILE_NAME_FINGERPRINT).toFile();
        return FileUtil.exist(marker) && fingerprint.equals(FileUtil.readUtf8String(marker));
    }

    /**
     * 物化单个 SKILL 的文件树到技能目录
     *
     * @return 是否包含根级 SKILL.md
     */
    private boolean materializeSkill(Path skillDirectory, Long skillId) {
        // 1. 查询 SKILL 的全部节点，构建编号映射，用于沿 parentId 解析相对路径
        // TODO DONE @AI：拿到后，就是 map；
        List<Ai1SkillFileDO> files = skillFileService.getSkillFileListBySkillId(skillId);
        Map<Long, Ai1SkillFileDO> fileMap = convertMap(files, Ai1SkillFileDO::getId);

        // 2. 逐个节点写入：文件写入内容（自动创建父目录），目录直接创建
        Map<Long, Path> pathCache = new HashMap<>();
        boolean hasSkillFile = false;
        for (Ai1SkillFileDO file : files) {
            File target = skillDirectory.resolve(resolveRelativePath(file, fileMap, pathCache)).toFile();
            if (Ai1SkillFileTypeEnum.isFile(file.getType())) {
                // 根级的 SKILL.md 是技能入口
                hasSkillFile |= Ai1SkillFileDO.NAME_SKILL.equals(file.getName())
                        && ObjUtil.equal(file.getParentId(), Ai1SkillFileDO.PARENT_ID_ROOT);
                FileUtil.writeUtf8String(StrUtil.nullToEmpty(file.getContent()), target);
            } else {
                FileUtil.mkdir(target);
            }
        }

        // 3. 缺少根级 SKILL.md 时，只物化文件，不作为技能注册
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
        Path path = parent == null || ObjUtil.equal(parent.getId(), file.getId())
                ? segment : resolveRelativePath(parent, fileMap, pathCache).resolve(segment);
        pathCache.put(file.getId(), path);
        return path;
    }

    private Path buildAgentRoot(Long agentId) {
        return Path.of(ai1Properties.getSkill().getRoot(), "agent_" + agentId);
    }

    /**
     * 路径净化：屏蔽路径分隔符与穿越片段
     */
    private static String sanitize(String name) {
        if (StrUtil.isBlank(name)) {
            return "_";
        }
        return StrUtil.replace(StrUtil.replaceChars(name, new char[]{'\\', '/'}, "_"), "..", "_");
    }

    /**
     * Agent 物化快照
     */
    @Data
    @AllArgsConstructor
    private static class SkillSnapshot {

        /**
         * 指纹
         */
        private final String fingerprint;
        /**
         * Agent 物化根目录
         */
        private final Path root;
        /**
         * 技能工具；无有效 SKILL.md 时为空
         */
        private final ToolCallback skillsTool;

    }

}
