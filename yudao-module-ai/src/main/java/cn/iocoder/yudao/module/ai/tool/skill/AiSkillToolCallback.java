package cn.iocoder.yudao.module.ai.tool.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 技能只读工具：read_skill 读取 SKILL.md，read_skill_file 读取技能内的文本文件
 *
 * 1. 每次读取都通过 {@link AiSkillService#readSkillFile(Long, String, String)} 重新校验角色绑定、技能启用、文件存在
 * 2. 只读：不物化到磁盘、不执行 scripts/ 下的内容
 *
 * @author 芋道源码
 */
@Slf4j
public class AiSkillToolCallback implements ToolCallback {

    /**
     * 工具名：读取 SKILL.md
     */
    public static final String NAME_READ_SKILL = "read_skill";
    /**
     * 工具名：读取技能内的文件
     */
    public static final String NAME_READ_SKILL_FILE = "read_skill_file";

    /**
     * 可用技能清单的最大长度
     */
    public static final int SKILL_PROMPT_MAX_LENGTH = 2000;

    private static final String READ_SKILL_INPUT_SCHEMA = "{\n"
            + "  \"type\": \"object\",\n"
            + "  \"properties\": {\n"
            + "    \"name\": { \"type\": \"string\", \"description\": \"技能名称，取自系统设定中的“可用技能”清单\" }\n"
            + "  },\n"
            + "  \"required\": [\"name\"]\n"
            + "}";
    private static final String READ_SKILL_FILE_INPUT_SCHEMA = "{\n"
            + "  \"type\": \"object\",\n"
            + "  \"properties\": {\n"
            + "    \"name\": { \"type\": \"string\", \"description\": \"技能名称，取自系统设定中的“可用技能”清单\" },\n"
            + "    \"path\": { \"type\": \"string\", \"description\": \"技能内文件的相对路径，例如 reference/guide.md\" }\n"
            + "  },\n"
            + "  \"required\": [\"name\", \"path\"]\n"
            + "}";

    private final ToolDefinition toolDefinition;
    /**
     * 角色编号
     */
    private final Long roleId;
    /**
     * 租户编号：流式回调中没有租户上下文，按请求开始时的租户读取
     */
    private final Long tenantId;
    /**
     * 本次回复的可用技能，仅用于生成系统设定中的清单
     */
    @Getter
    private final List<AiSkillDO> skills;

    private final AiSkillService skillService;

    private AiSkillToolCallback(ToolDefinition toolDefinition, Long roleId, Long tenantId, List<AiSkillDO> skills,
                                AiSkillService skillService) {
        this.toolDefinition = toolDefinition;
        this.roleId = roleId;
        this.tenantId = tenantId;
        this.skills = skills;
        this.skillService = skillService;
    }

    /**
     * 创建本次回复的技能工具：read_skill、read_skill_file
     *
     * @param roleId       角色编号
     * @param tenantId     租户编号
     * @param skills       角色绑定且启用的技能
     * @param skillService 技能 Service
     * @return 技能工具列表
     */
    public static List<ToolCallback> buildList(Long roleId, Long tenantId, List<AiSkillDO> skills,
                                               AiSkillService skillService) {
        ToolDefinition readSkill = ToolDefinition.builder().name(NAME_READ_SKILL)
                .description("读取技能的 SKILL.md 正文与文件列表。需要使用“可用技能”清单中的技能时，先调用本工具。")
                .inputSchema(READ_SKILL_INPUT_SCHEMA).build();
        ToolDefinition readSkillFile = ToolDefinition.builder().name(NAME_READ_SKILL_FILE)
                .description("读取技能内的文本文件，例如 SKILL.md 中提到的 reference/guide.md。path 为相对技能根目录的路径。")
                .inputSchema(READ_SKILL_FILE_INPUT_SCHEMA).build();
        return Arrays.asList(
                new AiSkillToolCallback(readSkill, roleId, tenantId, skills, skillService),
                new AiSkillToolCallback(readSkillFile, roleId, tenantId, skills, skillService));
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return call(toolInput);
    }

    @Override
    public String call(String toolInput) {
        // 1. 解析参数
        Map<String, Object> args = JsonUtils.parseMap(toolInput);
        String name = args != null ? MapUtil.getStr(args, "name") : null;
        String path = null;
        if (NAME_READ_SKILL_FILE.equals(toolDefinition.name())) {
            path = args != null ? MapUtil.getStr(args, "path") : null;
            if (StrUtil.isBlank(path)) {
                return "参数 path 不能为空，请传入技能内文件的相对路径，例如 reference/guide.md";
            }
        }

        // 2. 读取：按请求开始时的租户执行，每次都重新校验绑定、启用与文件
        String finalPath = path;
        try {
            return TenantUtils.execute(tenantId, () -> skillService.readSkillFile(roleId, name, finalPath));
        } catch (Exception ex) {
            log.error("[call][角色({}) 读取技能({}) 文件({}) 失败]", roleId, name, finalPath, ex);
            return "读取技能失败，请稍后重试";
        }
    }

    // ========== 可用技能清单 ==========

    /**
     * 在系统设定末尾追加可用技能清单：仅名称与描述，合计不超过 {@link #SKILL_PROMPT_MAX_LENGTH} 字
     *
     * @param systemMessage 角色设定
     * @param toolCallbacks 本次回复的工具列表；没有技能工具时，原样返回
     * @return 系统设定
     */
    public static String appendSkillPrompt(String systemMessage, List<ToolCallback> toolCallbacks) {
        AiSkillToolCallback skillToolCallback = findSkillToolCallback(toolCallbacks);
        if (skillToolCallback == null || CollUtil.isEmpty(skillToolCallback.getSkills())) {
            return systemMessage;
        }
        String skillPrompt = buildSkillPrompt(skillToolCallback.getSkills());
        return StrUtil.isBlank(systemMessage) ? skillPrompt : systemMessage + "\n\n" + skillPrompt;
    }

    /**
     * 是否包含技能工具
     *
     * @param toolCallbacks 工具列表
     * @return 是否包含
     */
    public static boolean contains(List<ToolCallback> toolCallbacks) {
        return findSkillToolCallback(toolCallbacks) != null;
    }

    private static AiSkillToolCallback findSkillToolCallback(List<ToolCallback> toolCallbacks) {
        return (AiSkillToolCallback) CollUtil.findOne(toolCallbacks, callback -> callback instanceof AiSkillToolCallback);
    }

    private static String buildSkillPrompt(List<AiSkillDO> skills) {
        StringBuilder skillList = new StringBuilder();
        int count = 0;
        for (AiSkillDO skill : skills) {
            String line = "- " + skill.getName() + "：" + StrUtil.nullToEmpty(skill.getDescription()) + "\n";
            if (skillList.length() + line.length() > SKILL_PROMPT_MAX_LENGTH) {
                break;
            }
            skillList.append(line);
            count++;
        }
        if (count < skills.size()) {
            skillList.append(StrUtil.format("- （清单长度受限，其余 {} 个技能未列出）\n", skills.size() - count));
        }
        return "## 可用技能\n"
                + "需要时先调用 read_skill 读取技能说明，再按说明调用 read_skill_file 读取所需文件，只读取必要的内容。\n"
                + skillList;
    }

}
