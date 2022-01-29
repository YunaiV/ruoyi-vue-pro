package cn.iocoder.yudao.module.system.mq.consumer.permission;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.system.mq.message.permission.SysMenuRefreshMessage;
import cn.iocoder.yudao.module.system.service.permission.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysMenuRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SysMenuRefreshConsumer extends AbstractChannelMessageListener<SysMenuRefreshMessage> {

    @Resource
    private SysMenuService menuService;

    @Override
    public void onMessage(SysMenuRefreshMessage message) {
        log.info("[onMessage][收到 Menu 刷新消息]");
        menuService.initLocalCache();
    }

}
