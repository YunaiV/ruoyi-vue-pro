package cn.iocoder.yudao.framework.ai.image;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.interactions.MjImagineInteractions;
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

    @Test
    public void mjImage() {
        String token =  "NTY5MDY4NDAxNzEyOTU1Mzky.G4-Fu0.MzD-7ll-ElbXTTgDPHF-WS_UyhMAfbKN3WyyBc";
        Map<String, String> requestTemplates = new HashMap<>();
        List<File> files = FileUtil.loopFiles("/Users/fansili/projects/github/ruoyi-vue-pro/yudao-module-ai/yudao-spring-boot-starter-ai/src/main/resources/http-body");
        for (File file : files) {
            requestTemplates.put(file.getName().replace(".json", ""), FileUtil.readUtf8String(file));
        }
        MidjourneyConfig midjourneyConfig = new MidjourneyConfig(
                token, "1221445697157468200", "1221445862962630706", requestTemplates);
        MjImagineInteractions mjImagineInteractions = new MjImagineInteractions(midjourneyConfig);
        mjImagineInteractions.execute("童话里应该是什么样子？");
    }
}
