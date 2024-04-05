package cn.iocoder.yudao.framework.ai.image;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.interactions.MjImagineInteractions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mj
 *
 * author: fansili
 * time: 2024/4/4 18:59
 */
public class MjTests {

    private MidjourneyConfig midjourneyConfig;
    @Before
    public void setup() {
        String token =  "OTcwNDc3NzQxMjUyMTY5NzI4.GJcVxa.VrzMii8dsHOJAPZn4Mw8GuEo7_nIUJij9JIHD4";
        Map<String, String> requestTemplates = new HashMap<>();
        List<File> files = FileUtil.loopFiles("/Users/fansili/projects/github/ruoyi-vue-pro/yudao-module-ai/yudao-spring-boot-starter-ai/src/main/resources/http-body");
        for (File file : files) {
            requestTemplates.put(file.getName().replace(".json", ""), FileUtil.readUtf8String(file));
        }
        this.midjourneyConfig = new MidjourneyConfig(token, "1225414986084388926", "1225414986587832385", requestTemplates);
    }

    @Test
    public void mjImage() {
        MjImagineInteractions mjImagineInteractions = new MjImagineInteractions(midjourneyConfig);
        mjImagineInteractions.execute("童话里应该是什么样子？");
    }
}
