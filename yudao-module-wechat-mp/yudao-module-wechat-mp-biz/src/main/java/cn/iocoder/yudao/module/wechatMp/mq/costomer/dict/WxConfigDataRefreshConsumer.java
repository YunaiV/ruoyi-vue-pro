package cn.iocoder.yudao.module.wechatMp.mq.costomer.dict;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.wechatMp.config.WxMpConfig;
import cn.iocoder.yudao.module.wechatMp.mq.message.dict.WxConfigDataRefreshMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link WxConfigDataRefreshMessage} 的消费者
 *
 * @author lyz
 */
@Component
@Slf4j
public class WxConfigDataRefreshConsumer extends AbstractChannelMessageListener<WxConfigDataRefreshMessage> {

    @Resource
    private WxMpConfig wxMpConfig;

    @Override
    public void onMessage(WxConfigDataRefreshMessage message) {
        log.info("[onMessage][收到 WxConfigData 刷新消息]");
        wxMpConfig.initWxConfig();
    }

}
