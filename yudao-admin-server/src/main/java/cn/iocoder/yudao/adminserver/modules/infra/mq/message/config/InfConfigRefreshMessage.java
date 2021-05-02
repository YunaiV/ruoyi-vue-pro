package cn.iocoder.yudao.adminserver.modules.infra.mq.message.config;

import cn.iocoder.yudao.framework.redis.core.pubsub.ChannelMessage;
import lombok.Data;

/**
 * 配置数据刷新 Message
 */
@Data
public class InfConfigRefreshMessage implements ChannelMessage {

    @Override
    public String getChannel() {
        return "infra.config.refresh";
    }

}
