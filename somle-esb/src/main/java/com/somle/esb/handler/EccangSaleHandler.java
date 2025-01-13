package com.somle.esb.handler;

import com.somle.ai.model.AiName;
import com.somle.ai.service.AiService;
import com.somle.eccang.model.EccangOrder;
import com.somle.esb.converter.EccangToErpConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/13$
 */
@Slf4j
@Component
public class EccangSaleHandler {

    @Autowired
    EccangToErpConverter eccangToErpConverter;

    @Autowired
    AiService aiService;

    @ServiceActivator(inputChannel = "saleChannel")
    public void handleSale(Message<EccangOrder> message) {
        var eccangOrder = message.getPayload();
        var erpSale = eccangToErpConverter.toEsb(eccangOrder);

        AiName name = AiName.builder()
            .name(erpSale.getCustomer().getName())
            .build();
        aiService.addName(name);
        aiService.addAddress(erpSale.getAddress());

    }
}
