package cn.iocoder.dashboard.modules.system.mq.consumer.sms;

import cn.iocoder.yudao.framework.redis.core.stream.AbstractStreamMessageListener;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysSmsSendMessage} 的消费者
 *
 * @author zzf
 * @date 2021/3/9 16:35
 */
@Component
@Slf4j
public class SysSmsSendConsumer extends AbstractStreamMessageListener<SysSmsSendMessage> {

    @Resource
    private SysSmsService smsService;

    @Override
    public void onMessage(SysSmsSendMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        smsService.doSendSms(message);
    }

}
