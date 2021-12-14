package cn.iocoder.yudao.adminserver.modules.infra.mq.message.config;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.Data;

/**
 * 配置数据刷新 Message
 */
@Data
public class InfConfigRefreshMessage extends AbstractChannelMessage {

    @Override
    public String getChannel() {
        return "infra.config.refresh";
    }

}
