package cn.iocoder.yudao.framework.ai.mj;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.listener.MjMessageListener;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.MjWebSocketStarter;
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
        String token =  "OTcyNzIxMzA0ODkxNDUzNDUw.G_vMOz.BO_Q0sXAD80u5ZKIHPNYDTRX_FgeKL3cKFc53I";
        Map<String, String> requestTemplates = new HashMap<>();
        List<File> files = FileUtil.loopFiles("/Users/fansili/projects/github/ruoyi-vue-pro/yudao-module-ai/yudao-spring-boot-starter-ai/src/main/resources/http-body");
        for (File file : files) {
            requestTemplates.put(file.getName().replace(".json", ""), FileUtil.readUtf8String(file));
        }
        this.midjourneyConfig = new MidjourneyConfig(token, "1225608134878302329", "1225608134878302332", requestTemplates);
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
