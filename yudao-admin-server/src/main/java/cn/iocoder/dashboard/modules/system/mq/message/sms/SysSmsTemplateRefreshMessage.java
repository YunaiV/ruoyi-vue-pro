package cn.iocoder.dashboard.modules.system.mq.message.sms;

import cn.iocoder.yudao.framework.redis.core.pubsub.ChannelMessage;
import lombok.Data;

/**
 * 短信模板的数据刷新 Message
 */
@Data
public class SysSmsTemplateRefreshMessage implements ChannelMessage {

    @Override
    public String getChannel() {
        return "system.sms-template.refresh";
    }

}
