package cn.iocoder.yudao.framework.ai.image;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.demo.wss.user.MjMessageListener;
import cn.iocoder.yudao.framework.ai.midjourney.demo.wss.user.MjWebSocketStarter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * author: fansili
 * time: 2024/4/3 16:40
 */
public class MjWebSocketTests {

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
    public void startSocketTest() {
        String wssUrl = "wss://gateway.discord.gg";
        var messageListener = new MjMessageListener(midjourneyConfig);
        var webSocketStarter = new MjWebSocketStarter(wssUrl, null, midjourneyConfig, messageListener);

        try {
            webSocketStarter.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
