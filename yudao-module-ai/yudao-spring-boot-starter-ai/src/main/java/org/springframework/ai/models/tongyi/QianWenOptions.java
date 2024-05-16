package org.springframework.ai.models.tongyi;

import org.springframework.ai.chat.prompt.ChatOptions;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 阿里云 千问 属性
 *
 * 地址：https://help.aliyun.com/document_detail/2684682.html?spm=a2c4g.2621347.0.0.195117e7Ytpkyo
 *
 * author: fansili
 * time: 2024/3/15 19:57
 */
@Data
@Accessors(chain = true)
public class QianWenOptions implements ChatOptions {

    /**
     * 用户与模型的对话历史
     */
    private List<Message> messages;
    /**
     * 生成时，核采样方法的概率阈值。例如，取值为0.8时，仅保留累计概率之和大于等于0.8的概率分布中的token，
     * 作为随机采样的候选集。取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的随机性越低。
     * 默认值为0.8。注意，取值不要大于等于1
     */
    private Float topP;
    /**
     * 用于限制模型生成token的数量，max_tokens设置的是生成上限，并不表示一定会生成这么多的token数量。其中qwen1.5-14b-chat、qwen1.5-7b-chat、qwen-14b-chat和qwen-7b-chat最大值和默认值均为1500，qwen-1.8b-chat、qwen-1.8b-longcontext-chat和qwen-72b-chat最大值和默认值均为2000
     */
    private Integer maxTokens = 1500;

    //
    // 适配 ChatOptions

    @Override
    public Float getTemperature() {
        return null;
    }

//    @Override
//    public void setTemperature(Float temperature) {
//
//    }
//
//    @Override
//    public void setTopP(Float topP) {
//        this.topP = topP;
//    }

    @Override
    public Integer getTopK() {
        return null;
    }

//    @Override
//    public void setTopK(Integer topK) {
//
//    }

    @Data
    @Accessors
    public static class Message {
        /**
         * 角色: system、user或assistant
         */
        private String role;
        /**
         * 提示词或模型内容
         */
        private String content;
    }

    @Data
    @Accessors
    public static class Parameters {
        /**
         * 输出格式, 默认为"text"
         * "text"表示旧版本的text
         * "message"表示兼容openai的message
         */
        private String resultFormat;
        /**
         * 生成时，采样候选集的大小。例如，取值为50时，仅将单次生成中得分最高的50个token组成随机采样的候选集。
         * 取值越大，生成的随机性越高；取值越小，生成的确定性越高。
         * 注意：如果top_k参数为空或者top_k的值大于100，表示不启用top_k策略，此时仅有top_p策略生效，默认是空。
         */
        private Integer topK;
        /**
         * 生成时使用的随机数种子，用户控制模型生成内容的随机性。
         * seed支持无符号64位整数，默认值为1234。在使用seed时，模型将尽可能生成相同或相似的结果，但目前不保证每次生成的结果完全相同。
         */
        private Integer seed;
        /**
         * 用于控制随机性和多样性的程度。具体来说，temperature值控制了生成文本时对每个候选词的概率分布进行平滑的程度。
         * 较高的temperature值会降低概率分布的峰值，使得更多的低概率词被选择，
         * 生成结果更加多样化；而较低的temperature值则会增强概率分布的峰值，使得高概率词更容易被选择，生成结果更加确定。
         * 取值范围： [0, 2)，系统默认值1.0。不建议取值为0，无意义。
         */
        private Float temperature;
        /**
         * 用于限制模型生成token的数量，max_tokens设置的是生成上限，并不表示一定会生成这么多的token数量。
         * 其中qwen-turbo 最大值和默认值为1500， qwen-max、qwen-max-1201 、qwen-max-longcontext 和 qwen-plus最大值和默认值均为2000。
         */
        private Integer maxTokens;
        /**
         * stop参数用于实现内容生成过程的精确控制，在生成内容即将包含指定的字符串或token_ids时自动停止，生成内容不包含指定的内容。
         * 例如，如果指定stop为"你好"，表示将要生成"你好"时停止；如果指定stop为[37763, 367]，表示将要生成"Observation"时停止。
         */
        private List<String> stop;
        /**
         * 用于控制流式输出模式，默认False，即后面内容会包含已经输出的内容；设置为True，将开启增量输出模式，
         * 后面输出不会包含已经输出的内容，您需要自行拼接整体输出，参考流式输出示例代码。
         */
        private Boolean incrementalOutput;
    }
}


