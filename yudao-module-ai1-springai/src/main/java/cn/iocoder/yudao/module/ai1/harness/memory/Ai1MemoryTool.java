package cn.iocoder.yudao.module.ai1.harness.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.service.memory.Ai1MemoryService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.util.Ai1Utils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MODEL_TYPE_NOT_CHAT;

/**
 * AI1 长期记忆工具（harness 运行时）
 *
 * 1. {@link #loadMemory}：读取用户在 Agent 下的记忆
 * 2. {@link #mergePrompt}：把记忆追加到系统指令，供对话注入
 * 3. {@link #generate}：以「历史记忆 + 最新对话」为输入，经模型按固定维度生成最新记忆
 *
 * 记忆的持久化由归档 {@link Ai1MemoryArchiveTool} 完成
 *
 * @author 芋道源码
 */
@Component
public class Ai1MemoryTool {

    /**
     * 记忆维度小节标题
     */
    private static final List<String> MEMORY_SECTIONS = List.of("用户画像", "偏好习惯", "关键事实", "目标与意图", "近期进展");

    /**
     * 记忆维度与生成约束（系统指令，{maxLen} 为长度上限占位符）
     */
    private static final String MEMORY_SYSTEM_PROMPT = """
            你是长期记忆归档助手。请基于【历史记忆】与【最新对话】，按下列维度抽取并更新用户长期记忆。

            输出格式（务必严格遵守）：
            1. 只输出 Markdown 记忆正文，不要任何解释、前言或代码块围栏；
            2. 每个维度标题独占一行，形如“## 维度名”（井号后有且仅有一个空格）；
            3. 每个要点独占一行，以“- ”开头；
            4. 标题与要点之间、要点与要点之间都必须换行，禁止把标题与内容写在同一行；
            5. 不同维度之间空一行。

            维度（无内容的维度整节省略）：
            ## 用户画像
            - 用户的身份、称呼、职业、所在地等基本画像
            ## 偏好习惯
            - 用户的沟通偏好、回答风格、常用语言与工具等
            ## 关键事实
            - 用户明确告知、需要长期记住的事实性信息
            ## 目标与意图
            - 用户的核心诉求、长期目标与待办事项
            ## 近期进展
            - 最近对话中已推进的事项、结论与下一步

            要求：合并历史记忆与新对话信息，删除重复与已过时内容，保持精炼；总长度不超过 {maxLen} 字。""";

    private static final String PLACEHOLDER_MAX_LEN = "{maxLen}";

    /**
     * 合并进系统指令时，记忆正文前的引导语
     */
    private static final String MEMORY_PROMPT_PREFIX = "\n\n以下是该用户的历史长期记忆（Markdown），请在回答时结合这些信息；"
            + "若与用户当前表述冲突，以当前表述为准：\n";

    @Resource
    private Ai1MemoryService memoryService;
    @Resource
    private Ai1ModelService modelService;

    @Resource
    private Ai1LlmChatTool llmChatTool;

    /**
     * 读取用户在 Agent 下的长期记忆
     *
     * @param agentId Agent 编号
     * @param userId  用户编号
     * @return 记忆正文；不存在时返回 null
     */
    public String loadMemory(Long agentId, Long userId) {
        if (agentId == null || userId == null) {
            return null;
        }
        Ai1MemoryDO memory = memoryService.getMemoryByAgentIdAndUserId(agentId, userId);
        return memory != null ? memory.getContent() : null;
    }

    /**
     * 把长期记忆合并进系统指令；记忆为空时原样返回
     *
     * @param basePrompt 系统指令
     * @param memory     记忆正文
     * @return 合并后的系统指令
     */
    public String mergePrompt(String basePrompt, String memory) {
        String prompt = StrUtil.nullToEmpty(basePrompt);
        if (StrUtil.isBlank(memory)) {
            return prompt;
        }
        return prompt + MEMORY_PROMPT_PREFIX + memory.trim();
    }

    /**
     * 经模型生成最新记忆：不装配历史、工具、RAG，也不注入已有记忆
     *
     * @param agent       记忆所属 Agent，使用其对话模型
     * @param oldMemory   历史记忆，可为空
     * @param newMessages 自上次归档以来的新消息，按编号升序
     * @param maxLength   记忆长度上限（字符）
     * @return 最新记忆；模型无输出时返回空
     */
    public String generate(Ai1AgentDO agent, String oldMemory, List<Ai1SessionMessageDO> newMessages, int maxLength) {
        // 1. 校验对话模型
        Ai1ModelRespBO model = modelService.getModelRespBO(agent.getProviderId(), agent.getModelId());
        if (!Ai1ModelTypeEnum.isChat(model.getModelType())) {
            throw exception(MODEL_TYPE_NOT_CHAT);
        }

        // 2. 单轮会话，聚合为完整文本
        int limit = maxLength > 0 ? maxLength : YudaoAi1Properties.Memory.DEFAULT_MAX_LENGTH;
        String systemPrompt = MEMORY_SYSTEM_PROMPT.replace(PLACEHOLDER_MAX_LEN, String.valueOf(limit));
        Ai1LlmChatTool.ChatText chatText = llmChatTool.chat(model, systemPrompt, Collections.emptyList(),
                buildUserPrompt(oldMemory, newMessages), Collections.emptyList(), Collections.emptyList(),
                null, null, null);

        // 3. 规范化格式并截断
        return Ai1Utils.limitLength(Ai1Utils.normalizeMemoryMarkdown(chatText != null ? chatText.getContent() : null,
                MEMORY_SECTIONS), limit);
    }

    /**
     * 组装归档的用户提示：历史记忆 + 最新对话
     */
    private static String buildUserPrompt(String oldMemory, List<Ai1SessionMessageDO> newMessages) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("【历史记忆】\n");
        prompt.append(StrUtil.isBlank(oldMemory) ? "（暂无）" : oldMemory.trim()).append("\n\n");
        prompt.append("【最新对话】\n");
        if (CollUtil.isNotEmpty(newMessages)) {
            for (Ai1SessionMessageDO message : newMessages) {
                String role = Ai1SessionMessageRoleEnum.isAssistant(message.getRole()) ? "助手" : "用户";
                prompt.append(role).append("：").append(StrUtil.nullToEmpty(message.getContent())).append("\n");
            }
        }
        return prompt.toString();
    }

}
