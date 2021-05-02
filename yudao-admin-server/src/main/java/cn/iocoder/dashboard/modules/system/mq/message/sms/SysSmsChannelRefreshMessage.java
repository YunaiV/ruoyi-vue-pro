package cn.iocoder.dashboard.modules.system.mq.message.sms;

import cn.iocoder.yudao.framework.redis.core.pubsub.ChannelMessage;
import lombok.Data;

/**
 * 短信渠道的数据刷新 Message
 */
@Data
public class SysSmsChannelRefreshMessage implements ChannelMessage {

    @Override
    public String getChannel() {
        return "system.sms-channel.refresh";
    }

}
