package com.somle.esb.handler;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
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
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @ServiceActivator(inputChannel = "erpProductChannel")
    public void syncProductsToEccang(@Payload List<ErpProductDTO> erpProductDTOS) {
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
        List<KingdeeProductSaveReqVO> kingdee = erpToKingdeeConverter.toKingdeeProducts(products);
        for (KingdeeProductSaveReqVO reqVO : kingdee) {
            kingdeeService.addProduct(reqVO);
        }
        log.info("syncProductsToKingdee end");
    }

}
