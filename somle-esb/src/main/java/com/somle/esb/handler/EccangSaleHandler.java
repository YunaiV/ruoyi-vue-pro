package com.somle.esb.handler;

import com.somle.ai.service.AiService;
import com.somle.esb.converter.EccangToErpConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/13$
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class EccangSaleHandler {

    private final EccangToErpConverter eccangToErpConverter;
    private final AiService aiService;

//    @ServiceActivator(inputChannel = "eccangSaleOutputChannel")
//    public void handleSale(Message<EccangOrder> message) {
//        var eccangOrder = message.getPayload();
//        var erpSale = eccangToErpConverter.toEsb(eccangOrder);
//
//        AiName name = AiName.builder()
//            .name(erpSale.getCustomer().getName())
//            .build();
//        aiService.addName(name);
//        aiService.addAddress(erpSale.getAddress());
//
//    }
}
