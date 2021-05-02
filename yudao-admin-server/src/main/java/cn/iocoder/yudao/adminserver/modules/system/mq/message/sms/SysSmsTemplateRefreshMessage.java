package cn.iocoder.yudao.adminserver.modules.system.mq.message.sms;

import cn.iocoder.yudao.framework.mq.core.pubsub.ChannelMessage;
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
