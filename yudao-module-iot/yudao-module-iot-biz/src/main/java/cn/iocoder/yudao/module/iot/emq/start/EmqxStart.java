package cn.iocoder.yudao.module.iot.emq.start;

import cn.iocoder.yudao.module.iot.emq.client.EmqxClient;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// TODO @芋艿：在瞅瞅

/**
 * 用于在应用启动时自动连接MQTT服务器
 *
 * @author ahh
 */
@Component
public class EmqxStart implements ApplicationRunner {

    @Resource
    private EmqxClient emqxClient;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        emqxClient.connect();
    }
}
