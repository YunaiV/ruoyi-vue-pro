package com.somle.esb.handler.erp;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.TmsCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.kingdee.model.KingdeeProductSaveReqVO;
import com.somle.kingdee.service.KingdeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/13$
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ErpProductHandler {

    private final KingdeeService kingdeeService;
    private final EccangService eccangService;
    private final ErpToEccangConverter erpToEccangConverter;
    private final ErpToKingdeeConverter erpToKingdeeConverter;
    private final MessageChannel erpCustomRuleChannel;
    private final TmsCustomRuleApi tmsCustomRuleApi;

    @ServiceActivator(inputChannel = "erpProductChannel")
    public void syncProductsToEccang(@Payload List<ErpProductDTO> erpProductDTOS) {
        erpProductDTOS.forEach(erpProductDTO -> syncProduct(erpProductDTO.getId()));
        List<EccangProduct> eccangProducts = erpToEccangConverter.convertByErpProducts(erpProductDTOS);
        for (EccangProduct eccangProduct : eccangProducts) {
            log.debug(eccangProduct.toString());
            eccangService.addBatchProduct(List.of(eccangProduct));
        }
        log.info("syncProductsToEccang end,sku = ({})", eccangProducts.stream().map(EccangProduct::getProductSku).toList());
    }

    @ServiceActivator(inputChannel = "erpProductChannel")
    public void syncProductsToKingdee(@Payload List<ErpProductDTO> products) {
        log.info("syncProductsToKingdee");
        products.forEach(erpProductDTO -> syncProduct(erpProductDTO.getId()));
        List<KingdeeProductSaveReqVO> kingdee = erpToKingdeeConverter.toKingdeeProducts(products);
        for (KingdeeProductSaveReqVO reqVO : kingdee) {
            kingdeeService.addProduct(reqVO);
        }
        log.info("syncProductsToKingdee end");
    }

    private void syncProduct(Long ErpProductDOId) {
        //更新产品时->覆盖n个海关规则
        //找到产品id对应的所有海关规则DTO(含海关信息+海关分类)，如果没有海关分类信息(产品逻辑必须有)，那么就不更新海关规则
        List<TmsCustomRuleDTO> dtos = new ArrayList<>();
        // 从产品ID获取海关规则 + 从分类ID获取海关规则
        Optional.ofNullable(tmsCustomRuleApi.listCustomRuleDTOsByProductId(ErpProductDOId)).ifPresent(dtos::addAll);
        if (!dtos.isEmpty()) {
            erpCustomRuleChannel.send(MessageBuilder.withPayload(dtos.stream().distinct().toList()).build());
        }
    }
}
