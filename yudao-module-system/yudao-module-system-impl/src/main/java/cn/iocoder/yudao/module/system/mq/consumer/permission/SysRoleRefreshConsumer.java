package cn.iocoder.yudao.module.system.mq.consumer.permission;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.system.mq.message.permission.SysRoleRefreshMessage;
import cn.iocoder.yudao.module.system.service.permission.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysRoleRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SysRoleRefreshConsumer extends AbstractChannelMessageListener<SysRoleRefreshMessage> {

    @Resource
    private SysRoleService roleService;

    @Override
    public void onMessage(SysRoleRefreshMessage message) {
        log.info("[onMessage][收到 Role 刷新消息]");
        roleService.initLocalCache();
    }

}
