package cn.iocoder.dashboard.modules.system.redis.mq.message.permission;

import cn.iocoder.dashboard.framework.redis.core.pubsub.ChannelMessage;
import lombok.Data;

/**
 * 角色与菜单数据刷新 Message
 */
@Data
public class SysRoleMenuRefreshMessage implements ChannelMessage {

    @Override
    public String getChannel() {
        return "system.role-menu.refresh";
    }

}
