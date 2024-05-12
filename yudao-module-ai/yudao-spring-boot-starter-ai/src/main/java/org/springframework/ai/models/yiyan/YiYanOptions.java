package org.springframework.ai.models.yiyan;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.models.yiyan.api.YiYanChatCompletionRequest;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 百度 问心一言
 *
 * 文档地址：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/clntwmv7t
 *
 * author: fansili
 * time: 2024/3/16 19:33
 */
@Data
@Accessors(chain = true)
public class YiYanOptions implements ChatOptions {

    /**
     * 一个可触发函数的描述列表，说明：
     * （1）支持的function数量无限制
     * （2）长度限制，最后一个message的content长度（即此轮对话的问题）、functions和system字段总内容不能超过20480 个字符，且不能超过5120 tokens
     * 必填：否
     */
    private List<YiYanChatCompletionRequest.Function> functions;
    /**
     * 说明：
     * （1）较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定
     * （2）默认0.8，范围 (0, 1.0]，不能为0
     * 必填：否
     */
    private Float temperature;
    /**
     * 说明：
     * （1）影响输出文本的多样性，取值越大，生成文本的多样性越强
     * （2）默认0.8，取值范围 [0, 1.0]
     * 必填：否
     */
    private Float topP;
    /**
     * 通过对已生成的token增加惩罚，减少重复生成的现象。说明：
     * （1）值越大表示惩罚越大
     * （2）默认1.0，取值范围：[1.0, 2.0]
     *
     * 必填：否
     */
    private Float penalty_score;
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
    private List<String> stop;
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
    private Integer maxOutputTokens;
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
     *
     * ERNIE-4.0-8K 模型没有这个字段
     */
    private String tool_choice;

    //
    // 以下兼容 spring-ai ChatOptions 暂时没有其他地方用到

    @Override
    public Float getTemperature() {
        return this.temperature;
    }

    @Override
    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    @Override
    public Float getTopP() {
        return topP;
    }

    @Override
    public void setTopP(Float topP) {
        this.topP = topP;
    }

    // 百度么有 topK

    @Override
    public Integer getTopK() {
        return null;
    }

    @Override
    public void setTopK(Integer topK) {
    }
}
