package com.somle.esb.job;

import com.somle.dingtalk.service.DingTalkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.DK_DEPARTMENT_CHANNEL;

/**
 * @className: syncDepartmentsJob
 * @author: Wqh
 * @date: 2024/10/29 17:24
 * @Version: 1.0
 * @description:
 */
@Slf4j
@Component
public class SyncDepartmentsJob extends DataJob {
    @Autowired
    DingTalkService dingTalkService;

    @Resource(name = DK_DEPARTMENT_CHANNEL)
    MessageChannel channel;


    @Override
    public String execute(String param) {
        dingTalkService.getDepartmentStream().forEach(department -> {
            channel.send(MessageBuilder.withPayload(department).build());
        });
        return "sync success";
    }
}
