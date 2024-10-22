package com.somle.esb.job;


import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.somle.ai.service.AiService;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class SyncUserJob extends DataJob {
    @Autowired
    EsbService service;


    @Override
    public String execute(String param) throws Exception {
        service.syncUsers();
        return "sync users success";
    }
}