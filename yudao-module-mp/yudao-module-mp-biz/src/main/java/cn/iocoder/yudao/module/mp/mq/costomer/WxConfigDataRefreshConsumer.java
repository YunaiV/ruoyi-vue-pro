package cn.iocoder.yudao.module.mp.mq.costomer;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.mp.mq.message.WxConfigDataRefreshMessage;
import cn.iocoder.yudao.module.mp.service.account.WxAccountService;
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
    private WxAccountService wxAccountService;

    @Override
    public void onMessage(WxConfigDataRefreshMessage message) {
        log.info("[onMessage][收到 WxConfigData 刷新消息]");
        wxAccountService.initLoadWxMpConfigStorages();
    }

}
