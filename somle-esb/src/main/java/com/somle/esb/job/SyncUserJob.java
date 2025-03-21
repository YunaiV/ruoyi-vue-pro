package com.somle.esb.job;

import com.somle.dingtalk.service.DingTalkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.DK_USER_CHANNEL;

@Slf4j
@Component
public class SyncUserJob extends DataJob {

    @Autowired
    DingTalkService dingTalkService;

    @Resource(name = DK_USER_CHANNEL)
    MessageChannel channel;

    @Override
    public String execute(String param) throws Exception {
        dingTalkService.getUserDetailStream().forEach(userDetail -> {
            channel.send(MessageBuilder.withPayload(userDetail).build());
        });
        return "sync users success";
    }
}