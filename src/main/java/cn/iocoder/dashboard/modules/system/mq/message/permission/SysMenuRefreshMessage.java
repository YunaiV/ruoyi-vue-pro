package cn.iocoder.dashboard.modules.system.mq.message.permission;

import lombok.Data;

/**
 * 菜单数据刷新 Message
 */
@Data
public class SysMenuRefreshMessage {

    public static final String TOPIC = "system.menu.refresh";

}
