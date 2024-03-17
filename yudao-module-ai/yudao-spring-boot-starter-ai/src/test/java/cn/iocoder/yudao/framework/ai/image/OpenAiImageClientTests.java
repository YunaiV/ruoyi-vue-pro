package cn.iocoder.yudao.framework.ai.image;

import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageApi;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageClient;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageOptions;
import org.junit.Before;
import org.junit.Test;

/**
 * author: fansili
 * time: 2024/3/17 10:40
 */
public class OpenAiImageClientTests {


    private OpenAiImageClient openAiImageClient;

    @Before
    public void setup() {
        // 初始化 openAiImageClient
        this.openAiImageClient = new OpenAiImageClient(
                new OpenAiImageApi(""),
                new OpenAiImageOptions()
        );
    }

    @Test
    public void callTest() {
        openAiImageClient.call(new ImagePrompt("我和我的小狗，一起在北极和企鹅玩排球。"));
    }
}
