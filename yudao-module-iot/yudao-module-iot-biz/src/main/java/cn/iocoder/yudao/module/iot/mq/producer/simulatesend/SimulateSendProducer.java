package cn.iocoder.yudao.module.iot.mq.producer.simulatesend;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SimulateSend 模拟设备上报的 Producer
 *
 * @author alwayssuper
 * @since 2024/12/17 16:35
 */
@Slf4j
@Component
public class SimulateSendProducer {
    @Resource
    private ApplicationContext applicationContext;

}
