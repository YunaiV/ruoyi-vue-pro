package cn.iocoder.yudao.module.mp.mq.producer;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.mp.mq.message.MpAccountRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 公众号账号 Producer
 *
 * @author 芋道源码
 */
@Component
public class MpAccountProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link MpAccountRefreshMessage} 消息
     */
    public void sendAccountRefreshMessage() {
        MpAccountRefreshMessage message = new MpAccountRefreshMessage();
        redisMQTemplate.send(message);
    }

}
