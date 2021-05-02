package cn.iocoder.yudao.adminserver.modules.infra.mq.consumer.config;

import cn.iocoder.yudao.framework.apollo.internals.DBConfigRepository;
import cn.iocoder.yudao.framework.redis.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.adminserver.modules.infra.mq.message.config.InfConfigRefreshMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 针对 {@link InfConfigRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class InfConfigRefreshConsumer extends AbstractChannelMessageListener<InfConfigRefreshMessage> {

    @Override
    public void onMessage(InfConfigRefreshMessage message) {
        log.info("[onMessage][收到 Config 刷新消息]");
        DBConfigRepository.noticeSync();
    }

}
