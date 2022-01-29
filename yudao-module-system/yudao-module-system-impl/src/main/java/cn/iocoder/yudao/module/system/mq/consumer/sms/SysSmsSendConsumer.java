package cn.iocoder.yudao.module.system.mq.consumer.sms;

import cn.iocoder.yudao.coreservice.modules.system.mq.message.sms.SysSmsSendMessage;
import cn.iocoder.yudao.coreservice.modules.system.service.sms.SysSmsCoreService;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessageListener;
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
    private SysSmsCoreService smsCoreService;

    @Override
    public void onMessage(SysSmsSendMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        smsCoreService.doSendSms(message);
    }

}
