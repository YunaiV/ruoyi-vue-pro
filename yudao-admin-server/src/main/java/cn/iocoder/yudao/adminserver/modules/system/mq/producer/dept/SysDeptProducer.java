package cn.iocoder.yudao.adminserver.modules.system.mq.producer.dept;

import cn.iocoder.yudao.framework.redis.core.util.RedisMessageUtils;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.dept.SysDeptRefreshMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Dept 部门相关消息的 Producer
 */
@Component
public class SysDeptProducer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送 {@link SysDeptRefreshMessage} 消息
     */
    public void sendDeptRefreshMessage() {
        SysDeptRefreshMessage message = new SysDeptRefreshMessage();
        RedisMessageUtils.sendChannelMessage(stringRedisTemplate, message);
    }

}
