package cn.iocoder.yudao.adminserver.modules.system.mq.producer.permission;

import cn.iocoder.yudao.adminserver.modules.system.mq.message.permission.SysMenuRefreshMessage;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Menu 菜单相关消息的 Producer
 */
@Component
public class SysMenuProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link SysMenuRefreshMessage} 消息
     */
    public void sendMenuRefreshMessage() {
        SysMenuRefreshMessage message = new SysMenuRefreshMessage();
        redisMQTemplate.send(message);
    }

}
