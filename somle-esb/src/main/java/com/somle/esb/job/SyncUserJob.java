package com.somle.esb.job;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.esb.enums.TenantId;
import com.somle.esb.handler.DingtalkUserHandler;
import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncUserJob extends DataJob {

    @Autowired
    DingTalkService dingTalkService;

    @Autowired
    MessageChannel dingtalkUserOutputChannel;

    @Override
    public String execute(String param) throws Exception {
        dingTalkService.getUserDetailStream().forEach(userDetail -> {
            dingtalkUserOutputChannel.send(MessageBuilder.withPayload(userDetail).build());
        });
        return "sync users success";
    }
}