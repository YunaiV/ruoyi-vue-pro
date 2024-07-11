package cn.iocoder.yudao.framework.ai.image;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.QianFanImageOptions;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.ai.qianfan.api.QianFanImageApi;

/**
 * 百度千帆 image
 */
public class QianFanImageTests {

    @Test
    public void callTest() {
        // todo @芋艿 千帆sdk有个错误，暂时没找到问题
        QianFanImageApi qianFanImageApi = new QianFanImageApi(
                "ghbbvbW2t7HK7WtYmEITAupm", "njJEr5AsQ5fkB3ucYYDjiQqsOZK20SGb");
        QianFanImageModel qianFanImageModel = new QianFanImageModel(qianFanImageApi);

        QianFanImageOptions imageOptions = QianFanImageOptions.builder()
                .withWidth(512)
                .withHeight(512)
                .build();
        ImagePrompt imagePrompt = new ImagePrompt("薄涂炫酷少女头像，田野花朵盛开", imageOptions);
        ImageResponse call = qianFanImageModel.call(imagePrompt);
        System.err.println(JsonUtils.toJsonString(call));
    }

    @Test
    public void call2Test() {
        // 官方测试 test https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-qianfan/src/test/java/org/springframework/ai/qianfan/image/QianFanImageModelIT.java
        var options = ImageOptionsBuilder.builder().withHeight(1024).withWidth(1024).build();
        var instructions = "薄涂炫酷少女头像，田野花朵盛开";

        ImagePrompt imagePrompt = new ImagePrompt(instructions, options);

        QianFanImageApi qianFanImageApi = new QianFanImageApi(
                "ghbbvbW2t7HK7WtYmEITAupm", "njJEr5AsQ5fkB3ucYYDjiQqsOZK20SGb");
        QianFanImageModel imageModel = new QianFanImageModel(qianFanImageApi);
        ImageResponse imageResponse = imageModel.call(imagePrompt);
    }

}
