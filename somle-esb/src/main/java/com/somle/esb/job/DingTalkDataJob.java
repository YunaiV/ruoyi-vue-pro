package com.somle.esb.job;

import com.somle.dingtalk.service.DingTalkService;
import com.somle.esb.model.Domain;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;

public class DingTalkDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    DingTalkService dingTalkService;

    final String DATABASE = Domain.DINGTALK.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}
