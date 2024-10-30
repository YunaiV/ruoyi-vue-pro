package com.somle.esb.job;


import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.ai.service.AiService;
import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.service.EccangService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EccangHeatMapJob implements JobHandler {

    @Autowired
    MessageChannel saleChannel;

    @Autowired
    EccangService eccangService;

    @Override
    public String execute(String param) throws Exception {
        var count = 0;
        var vo = EccangOrderVO.builder()
            .productSkuList(List.of(param))
            .build();
        for (var order : eccangService.getOrderPages(vo).toList()) {
            count++;
            saleChannel.send(MessageBuilder.withPayload(order).build());
        }


        return "sent count: " + count;
    }
}