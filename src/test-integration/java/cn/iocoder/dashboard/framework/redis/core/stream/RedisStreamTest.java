package cn.iocoder.dashboard.framework.redis.core.stream;

import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.dashboard.BaseRedisIntegrationTest;
import cn.iocoder.dashboard.framework.redis.core.util.RedisMessageUtils;
import cn.iocoder.dashboard.modules.system.mq.consumer.mail.SysMailSendConsumer;
import cn.iocoder.dashboard.modules.system.mq.consumer.sms.SysSmsSendConsumer;
import cn.iocoder.dashboard.modules.system.mq.message.mail.SysMailSendMessage;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

public class RedisStreamTest  {

    @Import({SysSmsSendConsumer.class, SysMailSendConsumer.class})
    public static class ConsumerTest extends BaseRedisIntegrationTest {

        @Test
        public void testConsumer() {
            ThreadUtil.sleep(1, TimeUnit.DAYS);
        }

    }

    public static class ProducerTest extends BaseRedisIntegrationTest {

        @Resource
        private StringRedisTemplate stringRedisTemplate;

        @Test
        public void testProducer01() {
            // 创建消息
            SysSmsSendMessage message = new SysSmsSendMessage();
            message.setMobile("15601691300").setTemplateCode("test");
            // 发送消息
            RedisMessageUtils.sendStreamMessage(stringRedisTemplate, message);
        }

        @Test
        public void testProducer02() {
            // 创建消息
            SysMailSendMessage message = new SysMailSendMessage();
            message.setAddress("fangfang@mihayou.com").setTemplateCode("test");
            // 发送消息
            RedisMessageUtils.sendStreamMessage(stringRedisTemplate, message);
        }

    }

}
