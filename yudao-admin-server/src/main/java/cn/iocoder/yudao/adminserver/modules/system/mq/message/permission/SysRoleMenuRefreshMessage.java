package cn.iocoder.yudao.adminserver.modules.system.mq.message.permission;

import cn.iocoder.yudao.framework.redis.core.pubsub.ChannelMessage;
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
