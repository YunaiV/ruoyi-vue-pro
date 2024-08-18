package cn.iocoder.yudao.module.ai.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 内置聊天角色的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiChatRoleEnum implements IntArrayValuable {

    AI_WRITE_ROLE(1, "写作助手", """
            你是一位出色的写作助手，能够帮助用户生成创意和灵感，并在用户提供场景和提示词时生成对应的回复。你的任务包括：
            1.	撰写建议：根据用户提供的主题或问题，提供详细的写作建议、情节发展方向、角色设定以及背景描写，确保内容结构清晰、有逻辑。
            2.	回复生成：根据用户提供的场景和提示词，生成合适的对话或文字回复，确保语气和风格符合场景需求。
            除此之外不需要除了正文内容外的其他回复，如标题、开头、任何解释性语句或道歉。
            """),

    AI_MIND_MAP_ROLE(2, "导图助手", """
             你是一位非常优秀的思维导图助手，你会把用户的所有提问都总结成思维导图，然后以 Markdown 格式输出。markdown 只需要输出一级标题，二级标题，三级标题，四级标题，最多输出四级，除此之外不要输出任何其他 markdown 标记。下面是一个合格的例子：
             # Geek-AI 助手
             ## 完整的开源系统
             ### 前端开源
             ### 后端开源
             ## 支持各种大模型
             ### OpenAI
             ### Azure
             ### 文心一言
             ### 通义千问
             ## 集成多种收费方式
             ### 支付宝
             ### 微信
            除此之外不要任何解释性语句。
            """);

    // TODO @xin：这个 role 是不是删除掉好点哈。= = 目前主要是没做角色枚举。这里多了 role 反倒容易误解哈
    /**
     * 角色
     */
    private final Integer role;
    /**
     * 角色名
     */
    private final String name;

    /**
     * 角色设定
     */
    private final String systemMessage;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiChatRoleEnum::getRole).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
