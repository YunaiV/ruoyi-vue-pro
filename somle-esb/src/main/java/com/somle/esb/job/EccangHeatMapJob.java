package com.somle.esb.job;


import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.service.EccangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EccangHeatMapJob implements JobHandler {

    @Autowired
    MessageChannel eccangSaleOutputChannel;

    @Autowired
    EccangService eccangService;

    @Override
    public String execute(String param) throws Exception {
        var count = 0;
        var vo = EccangOrderVO.builder()
            .condition(EccangOrderVO.Condition
                .builder()
                .productSkuList(List.of(param))
                .build())
            .build();
        for (var order : eccangService.getOrderPlusArchiveSince(vo, 2022).toList()) {
            count++;
            try {
                eccangSaleOutputChannel.send(MessageBuilder.withPayload(order).build());
            } catch (MessageHandlingException e) {
                Throwable rootCause = e.getCause();
                throw new RuntimeException(rootCause);
            }
        }


        return "sent count: " + count;
    }
}