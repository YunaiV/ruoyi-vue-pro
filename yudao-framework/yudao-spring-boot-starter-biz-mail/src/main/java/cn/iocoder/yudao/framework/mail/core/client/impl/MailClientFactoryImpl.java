package cn.iocoder.yudao.framework.mail.core.client.impl;

import cn.iocoder.yudao.framework.mail.core.client.MailClient;
import cn.iocoder.yudao.framework.mail.core.client.impl.hutool.HutoolMailClient;
import lombok.extern.slf4j.Slf4j;
import cn.iocoder.yudao.framework.mail.core.client.MailClientFactory;
import cn.iocoder.yudao.framework.mail.core.enums.MailChannelEnum;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
@Validated
@Slf4j
public class MailClientFactoryImpl implements MailClientFactory {

    private final ConcurrentMap<String, AbstractMailClient> channelCodeClients = new ConcurrentHashMap<>();

    public MailClientFactoryImpl (){
        Arrays.stream(MailChannelEnum.values()).forEach(mailChannelEnum -> {
            AbstractMailClient abstractMailClient = createMailClient(mailChannelEnum);
            channelCodeClients.put(mailChannelEnum.getCode() , abstractMailClient);
        });
    }

    private AbstractMailClient createMailClient(MailChannelEnum mailChannelEnum) {
        switch (mailChannelEnum){
            case HUTOOL: return new HutoolMailClient();
        }
        // 创建失败，错误日志 + 抛出异常
        log.error("[createMailClient][配置({}) 找不到合适的客户端实现]" , mailChannelEnum);
        throw new IllegalArgumentException(String.format("配置(%s) 找不到合适的客户端实现", mailChannelEnum));
    }

    @Override
    public MailClient getMailClient() {
        return channelCodeClients.get("HUTOOL");
    }
}
