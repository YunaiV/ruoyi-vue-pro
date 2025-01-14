package com.somle.esb.handler;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.kingdee.model.KingdeeProduct;
import com.somle.kingdee.service.KingdeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
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
public class ErpProductHandler {

    @Autowired
    KingdeeService kingdeeService;

    @Autowired
    EccangService eccangService;

    @Autowired
    ErpToEccangConverter erpToEccangConverter;

    @Autowired
    ErpToKingdeeConverter erpToKingdeeConverter;

    @ServiceActivator(inputChannel = "erpProductChannel")
    public void syncProductsToEccang(@Payload List<ErpProductDTO> products) {
        log.info("syncProductsToEccang");
        List<EccangProduct> eccangProducts = erpToEccangConverter.productDTOToProduct(products);
        for (EccangProduct eccangProduct : eccangProducts){
            eccangProduct.setActionType("ADD");
            EccangProduct eccangServiceProduct = eccangService.getProduct(eccangProduct.getProductSku());
            //根据sku从eccang中获取产品，如果产品不为空，则表示已存在，操作则变为修改
            if (ObjUtil.isNotEmpty(eccangServiceProduct)){
                eccangProduct.setActionType("EDIT");
                //如果是修改就要上传默认采购单价
                //TODO 后续有变更，请修改
                eccangProduct.setProductPurchaseValue(0.001F);
            }
            log.debug(eccangProduct.toString());
            eccangService.addBatchProduct(List.of(eccangProduct));
        }
        log.info("syncProductsToEccang end");
    }

    @ServiceActivator(inputChannel = "erpProductChannel")
    public void syncProductsToKingdee(@Payload List<ErpProductDTO> products) {
        log.info("syncProductsToKingdee");
        List<KingdeeProduct> kingdee = erpToKingdeeConverter.productDTOToProduct(products);
        for (KingdeeProduct kingdeeProduct : kingdee){
            kingdeeService.addProduct(kingdeeProduct);
        }
        log.info("syncProductsToKingdee end");
    }

}
