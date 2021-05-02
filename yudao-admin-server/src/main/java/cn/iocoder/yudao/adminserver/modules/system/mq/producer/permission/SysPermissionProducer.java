package cn.iocoder.yudao.adminserver.modules.system.mq.producer.permission;

import cn.iocoder.yudao.framework.redis.core.util.RedisMessageUtils;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.permission.SysRoleMenuRefreshMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Permission 权限相关消息的 Producer
 */
@Component
public class SysPermissionProducer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送 {@link SysRoleMenuRefreshMessage} 消息
     */
    public void sendRoleMenuRefreshMessage() {
        SysRoleMenuRefreshMessage message = new SysRoleMenuRefreshMessage();
        RedisMessageUtils.sendChannelMessage(stringRedisTemplate, message);
    }

}
