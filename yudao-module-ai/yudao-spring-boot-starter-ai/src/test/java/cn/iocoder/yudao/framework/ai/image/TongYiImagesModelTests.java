package cn.iocoder.yudao.framework.ai.image;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Map;

// TODO @fan：改成 TongYiImagesModel 哈
/**
 * 通义万象
 */
public class TongYiImagesModelTests {

    @Test
    public void imageCallTest() throws NoApiKeyException {
        // 设置 api key
        Constants.apiKey = "sk-Zsd81gZYg7";
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .model(ImageSynthesis.Models.WANX_V1)
                        .n(4)
                        .size("1024*1024")
                        .prompt("雄鹰自由自在的在蓝天白云下飞翔")
                        .build();
        // 创建 ImageSynthesis
        ImageSynthesis is = new ImageSynthesis();
        // 调用 call 生成 image
        ImageSynthesisResult call = is.call(param);
        System.err.println(JSON.toJSON(call));
        for (Map<String, String> result : call.getOutput().getResults()) {
            System.err.println("地址: " + result.get("url"));
        }
    }
}
