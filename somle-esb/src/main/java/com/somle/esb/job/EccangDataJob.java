package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.amazon.service.AmazonService;
import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.service.EccangService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.framework.common.util.general.CoreUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class EccangDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    EccangService eccangService;

    final String DATABASE = Domain.ECCANG.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}