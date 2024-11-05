package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.ai.service.AiService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.matomo.service.MatomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.IntStream;

@Component
public class AiDataJob extends DataJob {
    @Autowired
    EsbService service;

    @Autowired
    AiService aiService;

    final String DATABASE = Domain.AI.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}