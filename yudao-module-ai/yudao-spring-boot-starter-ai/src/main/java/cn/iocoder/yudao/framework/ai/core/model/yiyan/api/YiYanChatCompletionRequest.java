package cn.iocoder.yudao.framework.ai.core.model.yiyan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 文心一言 Completion Request
 *
 * 百度千帆文档：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/jlil56u11
 *
 * @author fansili
 */
@Data
public class YiYanChatCompletionRequest {

    public YiYanChatCompletionRequest(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * 聊天上下文信息
     */
    private List<Message> messages;
    /**
     * functions 函数
     */
    private List<Function> functions;
    /**
     * temperature
     */
    private Float temperature;
    /**
     * topP
     */
    @JsonProperty("top_p")
    private Float topP;
    /**
     * 通过对已生成的token增加惩罚，减少重复生成的现象
     */
    @JsonProperty("penalty_score")
    private Float penaltyScore;
    /**
     * stream 模式
     */
    private Boolean stream;
    /**
     * system 预设角色
     */
    private String system;
    /**
     * 生成停止标识，当模型生成结果以stop中某个元素结尾时，停止文本生成
     */
    private List<String> stop;
    /**
     * 是否强制关闭实时搜索功能
     */
    @JsonProperty("disable_search")
    private Boolean disableSearch;
    /**
     * 是否开启上角标返回
     */
    @JsonProperty("enable_citation")
    private Boolean enableCitation;
    /**
     * 最大输出 token 数
     */
    @JsonProperty("max_output_tokens")
    private Integer maxOutputTokens;
    /**
     * 返回格式 text、json_object
     */
    @JsonProperty("response_format")
    private String responseFormat;
    /**
     * 用户 id
     */
    @JsonProperty("user_id")
    private String userId;
    /**
     * 在函数调用场景下，提示大模型选择指定的函数（非强制），说明：指定的函数名必须在functions中存在
     * tip: ERNIE-4.0-8K 模型没有这个字段
     */
    @JsonProperty("tool_choice")
    private String toolChoice;


    @Data
    public static class Message {

        private String role;

        private String content;

    }

    @Data
    public static class ToolChoice {
        /**
         * 	指定工具类型，function
         * 	必填: 是
         */
        private String type;
        /**
         * 指定要使用的函数
         * 必填: 是
         */
        private Function function;
        /**
         * 指定要使用的函数名
         * 必填: 是
         */
        private String name;
    }

    @Data
    public static class Function {
        /**
         * 函数名
         * 必填: 是
         */
        private String name;
        /**
         * 函数描述
         * 必填: 是
         */
        private String description;
        /**
         * 函数请求参数，说明：
         * （1）JSON Schema 格式，参考JSON Schema描述
         * （2）如果函数没有请求参数，parameters值格式如下：
         * {"type": "object","properties": {}}
         * 必填: 是
         */
        private String parameters;
        /**
         * 函数响应参数，JSON Schema 格式，参考JSON Schema描述
         * 必填: 否
         */
        private String responses;
        /**
         * function调用的一些历史示例，说明：
         * （1）可以提供正例（正常触发）和反例（无需触发）的example
         * ·正例：从历史请求数据中获取
         * ·反例：
         *        当role = user，不会触发请求的query
         *        当role = assistant，有固定的格式。function_call的name为空，arguments是空对象:"{}"，thought可以填固定的:"我不需要调用任何工具"
         * （2）兼容之前的 List(example) 格式
         */
        private String examples;
    }

}
