package cn.iocoder.yudao.module.system.mq.consumer.permission;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.system.mq.message.permission.SysRoleMenuRefreshMessage;
import cn.iocoder.yudao.module.system.service.permission.SysPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysRoleMenuRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SysRoleMenuRefreshConsumer extends AbstractChannelMessageListener<SysRoleMenuRefreshMessage> {

    @Resource
    private SysPermissionService permissionService;

    @Override
    public void onMessage(SysRoleMenuRefreshMessage message) {
        log.info("[onMessage][收到 Role 与 Menu 的关联刷新消息]");
        permissionService.initLocalCache();
    }

}
