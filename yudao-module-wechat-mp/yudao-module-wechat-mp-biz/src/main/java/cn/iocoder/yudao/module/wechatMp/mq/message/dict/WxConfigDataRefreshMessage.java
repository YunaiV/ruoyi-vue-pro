package cn.iocoder.yudao.module.wechatMp.mq.message.dict;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信配置数据刷新 Message
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxConfigDataRefreshMessage extends AbstractChannelMessage {

    @Override
    public String getChannel() {
        return "wechat-mp.wx-config-data.refresh";
    }

}
