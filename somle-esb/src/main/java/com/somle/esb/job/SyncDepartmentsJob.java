package com.somle.esb.job;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.somle.dingtalk.config.DingtalkIntegrationConfig;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.esb.enums.TenantId;
import com.somle.esb.handler.DingtalkDepartmentHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * @className: syncDepartmentsJob
 * @author: Wqh
 * @date: 2024/10/29 17:24
 * @Version: 1.0
 * @description:
 */
@Slf4j
@Component
public class SyncDepartmentsJob extends DataJob{
    @Autowired
    DingTalkService dingTalkService;

    @Resource
    MessageChannel dingtalkDepartmentOutputChannel;


    @Override
    public String execute(String param){
        dingTalkService.getDepartmentStream().forEach(department -> {
            dingtalkDepartmentOutputChannel.send(MessageBuilder.withPayload(department).build());
        });
        return "sync success";
    }
}
