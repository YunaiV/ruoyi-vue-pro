package cn.iocoder.yudao.adminserver.modules.system.mq.message.dept;

import cn.iocoder.yudao.framework.mq.core.pubsub.ChannelMessage;
import lombok.Data;

/**
 * 部门数据刷新 Message
 */
@Data
public class SysDeptRefreshMessage implements ChannelMessage {

    @Override
    public String getChannel() {
        return "system.dept.refresh";
    }

}
