package cn.iocoder.yudao.module.mp.mq.consumer;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.mp.mq.message.MpConfigRefreshMessage;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link MpConfigRefreshMessage} 的消费者
 *
 * @author lyz
 */
@Component
@Slf4j
public class MpConfigRefreshConsumer extends AbstractChannelMessageListener<MpConfigRefreshMessage> {

    @Resource
    private MpAccountService wxAccountService;

    @Override
    public void onMessage(MpConfigRefreshMessage message) {
        log.info("[onMessage][收到 MpConfig 刷新消息]");
        wxAccountService.initLocalCache();
    }

}
