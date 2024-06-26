package cn.iocoder.yudao.framework.ai.core.model.yiyan;

import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanChatCompletionRequest;
import lombok.Data;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.List;

/**
 * 文心一言的 {@link ChatOptions} 实现类
 *
 * 字段说明：参考 <a href="https://cloud.baidu.com/doc/WENXINWORKSHOP/s/clntwmv7t">ERNIE-4.0-8K</a>
 *
 * @author fansili
 */
@Data
public class YiYanChatOptions implements ChatOptions {

    /**
     * functions 函数
     */
    private List<YiYanChatCompletionRequest.Function> functions;
    /**
     * temperature
     */
    private Float temperature;
    /**
     * topP
     */
    private Float topP;
    /**
     * 通过对已生成的token增加惩罚，减少重复生成的现象
     */
    private Float penaltyScore;
    /**
     * stream 模式请求
     */
    private Boolean stream;
    /**
     * system 提示
     */
    private String system;
    /**
     * 生成停止标识，当模型生成结果以stop中某个元素结尾时，停止文本生成
     */
    private List<String> stop;
    /**
     * 是否强制关闭实时搜索功能
     */
    private Boolean disableSearch;
    /**
     * 是否开启上角标返回
     */
    private Boolean enableCitation;
    /**
     * 输出最大 token
     */
    private Integer maxOutputTokens;
    /**
     * 响应格式 text、json_object
     */
    private String responseFormat;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 在函数调用场景下，提示大模型选择指定的函数（非强制），说明：指定的函数名必须在functions中存在
     * tip: ERNIE-4.0-8K 模型没有这个字段
     */
    private String toolChoice;

    @Override
    public Float getTemperature() {
        return this.temperature;
    }

    @Override
    public Float getTopP() {
        return topP;
    }

    /**
     * 百度么有 topK
     */
    @Override
    public Integer getTopK() {
        return null;
    }

}
