package cn.iocoder.yudao.framework.ai.core.model.yiyan.api;

import lombok.Data;

/**
 * 文心一言 Completion Response
 *
 * 百度链接: https://cloud.baidu.com/doc/WENXINWORKSHOP/s/clntwmv7t
 *
 * @author fansili
 */
@Data
public class YiYanChatCompletionResponse {

    /**
     * 本轮对话的id
     */
    private String id;
    /**
     * 回包类型，chat.completion：多轮对话返回
     */
    private String object;
    /**
     * 时间戳
     */
    private int created;
    /**
     * 表示当前子句的序号。只有在流式接口模式下会返回该字段
     */
    private int sentence_id;
    /**
     * 表示当前子句是否是最后一句。只有在流式接口模式下会返回该字段
     */
    private boolean is_end;
    /**
     * 当前生成的结果是否被截断
     */
    private boolean is_truncated;
    /**
     * 输出内容标识，说明：
     * · normal：输出内容完全由大模型生成，未触发截断、替换
     * · stop：输出结果命中入参stop中指定的字段后被截断
     * · length：达到了最大的token数，根据EB返回结果is_truncated来截断
     * · content_filter：输出内容被截断、兜底、替换为**等
     */
    private String finish_reason;
    /**
     * 搜索数据，当请求参数enable_citation为true并且触发搜索时，会返回该字段
     */
    private String search_info;
    /**
     * 对话返回结果
     */
    private String result;
    /**
     * 表示用户输入是否存在安全，是否关闭当前会话，清理历史会话信息
     * true：是，表示用户输入存在安全风险，建议关闭当前会话，清理历史会话信息
     * false：否，表示用户输入无安全风险
     */
    private boolean need_clear_history;
    /**
     * 说明：
     * · 0：正常返回
     * · 其他：非正常
     */
    private int flag;
    /**
     * 当need_clear_history为true时，此字段会告知第几轮对话有敏感信息，如果是当前问题，ban_round=-1
     */
    private int ban_round;
    /**
     * token统计信息
     */
    private Usage usage;

    @Data
    public static class Usage {
        /**
         * 问题tokens数
         */
        private int prompt_tokens;
        /**
         * 回答tokens数
         */
        private int completion_tokens;
        /**
         * tokens总数
         */
        private int total_tokens;
    }

}
