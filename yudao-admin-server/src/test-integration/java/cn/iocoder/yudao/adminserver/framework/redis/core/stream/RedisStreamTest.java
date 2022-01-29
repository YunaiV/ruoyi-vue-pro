package cn.iocoder.yudao.adminserver.framework.redis.core.stream;

import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.adminserver.BaseRedisIntegrationTest;
import cn.iocoder.yudao.module.system.mq.consumer.mail.SysMailSendConsumer;
import cn.iocoder.yudao.module.system.mq.consumer.sms.SysSmsSendConsumer;
import cn.iocoder.yudao.module.system.mq.message.mail.SysMailSendMessage;
import cn.iocoder.yudao.module.system.mq.message.sms.SysSmsSendMessage;
import cn.iocoder.yudao.framework.mq.core.util.RedisMessageUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

public class RedisStreamTest  {

    @Import({SysSmsSendConsumer.class, SysMailSendConsumer.class})
    @Disabled
    public static class ConsumerTest extends BaseRedisIntegrationTest {

        @Test
        public void testConsumer() {
            ThreadUtil.sleep(1, TimeUnit.DAYS);
        }

    }

    @Disabled
    public static class ProducerTest extends BaseRedisIntegrationTest {

        @Resource
        private StringRedisTemplate stringRedisTemplate;

        @Resource
        private RedisTemplate<String, Object> redisTemplate;

        @Test
        public void testProducer01() {
            for (int i = 0; i < 100; i++) {
                // 创建消息
                SysSmsSendMessage message = new SysSmsSendMessage();
                message.setMobile("15601691300").setApiTemplateId("test:" + i);
                // 发送消息
                RedisMessageUtils.sendStreamMessage(stringRedisTemplate, message);
            }
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
