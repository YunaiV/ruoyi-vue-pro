package cn.iocoder.yudao.framework.ai.chatyiyan.api;

import lombok.Data;

import java.util.List;

/**
 * 一言 Completion req
 *
 *  百度千帆文档：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/jlil56u11
 *
 * author: fansili
 * time: 2024/3/9 10:34
 */
@Data
public class YiYanChatCompletionRequest {

    public YiYanChatCompletionRequest(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * 聊天上下文信息。
     * 必填：是
     */
    private List<Message> messages;
    /**
     * 一个可触发函数的描述列表，说明：
     * （1）支持的function数量无限制
     * （2）长度限制，最后一个message的content长度（即此轮对话的问题）、functions和system字段总内容不能超过20480 个字符，且不能超过5120 tokens
     * 必填：否
     */
    private List<Function> functions;
    /**
     * 说明：
     * （1）较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定
     * （2）默认0.8，范围 (0, 1.0]，不能为0
     * 必填：否
     */
    private String temperature;
    /**
     * 说明：
     * （1）影响输出文本的多样性，取值越大，生成文本的多样性越强
     * （2）默认0.8，取值范围 [0, 1.0]
     * 必填：否
     */
    private String top_p;
    /**
     * 通过对已生成的token增加惩罚，减少重复生成的现象。说明：
     * （1）值越大表示惩罚越大
     * （2）默认1.0，取值范围：[1.0, 2.0]
     *
     * 必填：否
     */
    private String penalty_score;
    /**
     * 是否以流式接口的形式返回数据，默认false
     * 必填：否
     */
    private Boolean stream;
    /**
     * 模型人设，主要用于人设设定，例如，你是xxx公司制作的AI助手，说明：
     * （1）长度限制，最后一个message的content长度（即此轮对话的问题）、functions和system字段总内容不能超过20480 个字符，且不能超过5120 tokens
     * （2）如果同时使用system和functions，可能暂无法保证使用效果，持续进行优化
     * 必填：否
     */
    private String system;
    /**
     * 生成停止标识，当模型生成结果以stop中某个元素结尾时，停止文本生成。说明：
     * （1）每个元素长度不超过20字符
     * （2）最多4个元素
     * 必填：否
     */
    private String stop;
    /**
     * 是否强制关闭实时搜索功能，默认false，表示不关闭
     * 必填：否
     */
    private Boolean disable_search;
    /**
     * 是否开启上角标返回，说明：
     * （1）开启后，有概率触发搜索溯源信息search_info，search_info内容见响应参数介绍
     * （2）默认false，不开启
     * 必填：否
     */
    private Boolean enable_citation;
    /**
     * 指定模型最大输出token数，范围[2, 2048]
     * 必填：否
     */
    private Integer max_output_tokens;
    /**
     * 指定响应内容的格式，说明：
     * （1）可选值：
     * · json_object：以json格式返回，可能出现不满足效果情况
     * · text：以文本格式返回
     * （2）如果不填写参数response_format值，默认为text
     * 必填：否
     */
    private String response_format;
    /**
     * 表示最终用户的唯一标识符
     * 必填：否
     */
    private String user_id;
    /**
     * 在函数调用场景下，提示大模型选择指定的函数（非强制），说明：指定的函数名必须在functions中存在
     * 必填：否
     */
    private String tool_choice;


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
