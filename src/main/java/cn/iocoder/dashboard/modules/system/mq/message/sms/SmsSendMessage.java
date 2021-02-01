package cn.iocoder.dashboard.modules.system.mq.message.sms;

import cn.iocoder.dashboard.framework.redis.core.pubsub.ChannelMessage;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * 部门数据刷新 Message
 */
@Data
public class SmsSendMessage implements ChannelMessage {

    private SmsBody smsBody;

    private List<String> targetPhones;

    @Override
    public String getChannel() {
        return "sms.send";
    }

}
