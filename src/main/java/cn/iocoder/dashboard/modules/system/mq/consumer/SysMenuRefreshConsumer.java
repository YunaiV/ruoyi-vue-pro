package cn.iocoder.dashboard.modules.system.mq.consumer;

import cn.iocoder.dashboard.framework.redis.core.listener.AbstractMessageListener;
import cn.iocoder.dashboard.modules.system.mq.message.permission.SysMenuRefreshMessage;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class SysMenuRefreshConsumer extends AbstractMessageListener<SysMenuRefreshMessage> {

    @Resource
    private SysMenuService menuService;

    @Override
    public void onMessage(SysMenuRefreshMessage message) {
        log.info("[onMessage][收到 Menu 刷新消息]");
        menuService.init();
    }

    @Override
    public String getChannel() {
        return SysMenuRefreshMessage.TOPIC;
    }

}
