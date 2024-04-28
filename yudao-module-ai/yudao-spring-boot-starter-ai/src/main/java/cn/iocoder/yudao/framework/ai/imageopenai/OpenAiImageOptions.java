package cn.iocoder.yudao.framework.ai.imageopenai;

import cn.iocoder.yudao.framework.ai.image.ImageOptions;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * open ai 配置文件
 *
 * 文档地址：https://platform.openai.com/docs/api-reference/images/create
 *
 * author: fansili
 * time: 2024/3/17 09:53
 */
@Data
@Accessors(chain = true)
public class OpenAiImageOptions implements ImageOptions {

    // 必填字段，用于描述期望生成图像的文字说明。对于dall-e-2模型最大长度为1000个字符，对于dall-e-3模型最大长度为4000个字符。
    private String prompt;

    // 可选字段，默认为dall-e-2
    // 指定用于生成图像的模型名称。
    private OpenAiImageModelEnum model = OpenAiImageModelEnum.DALL_E_2;

    // 可选字段，默认为1
    // 生成图像的数量，必须在1到10之间。对于dall-e-3模型，目前仅支持n=1。
    private Integer n = 1;

    // 可选字段，默认为standard
    // 设置生成图像的质量。hd质量将创建细节更丰富、图像整体一致性更高的图片。该参数仅对dall-e-3模型有效。
    private String quality = "standard";

    // 可选字段，默认为url
    // 返回生成图像的格式。必须是url或b64_json中的一种。URL链接的有效期是从生成图像后开始计算的60分钟内有效。
    private String responseFormat = "url";

    // 可选字段，默认为1024x1024
    // 生成图像的尺寸大小。对于dall-e-2模型，尺寸可为256x256, 512x512, 或 1024x1024。对于dall-e-3模型，尺寸可为1024x1024, 1792x1024, 或 1024x1792。
    private String size = "1024x1024";

    // 可选字段，默认为vivid
    // 图像生成的风格。可为vivid（生动）或natural（自然）。vivid会使模型偏向生成超现实和戏剧性的图像，而natural则会让模型产出更自然、不那么超现实的图像。该参数仅对dall-e-3模型有效。
    private OpenAiImageStyleEnum style = OpenAiImageStyleEnum.VIVID;

    // 可选字段
    // 代表您的终端用户的唯一标识符，有助于OpenAI监控并检测滥用行为。了解更多信息请参考官方文档。
    private String endUserId = "UID123456";

    @Getter
    public enum ResponseFormatEnum {

        URL("url"),
        BASE64("b64_json"),

        ;

        ResponseFormatEnum(String value) {
            this.value = value;
        }

        private String value;
    }

    //
    // 适配 spring ai

    @Override
    public Integer getN() {
        return this.n;
    }

    @Override
    public String getModel() {
        return this.model.getModel();
    }

    @Override
    public Integer getWidth() {
        return null;
    }

    @Override
    public Integer getHeight() {
        return null;
    }

    @Override
    public String getResponseFormat() {
        return this.responseFormat;
    }
}
