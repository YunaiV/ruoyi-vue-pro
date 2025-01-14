package com.somle.esb.handler;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierProductService;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.converter.ErpToKingdeeConverter;
import com.somle.kingdee.model.KingdeeProduct;
import com.somle.kingdee.service.KingdeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/13$
 */
@Slf4j
@Component
public class ErpCustomRuleHandler {

    @Autowired
    KingdeeService kingdeeService;

    @Autowired
    EccangService eccangService;

    @Autowired
    ErpSupplierProductService erpSupplierProductService;
    @Autowired
    ErpToEccangConverter erpToEccangConverter;

    @Autowired
    ErpToKingdeeConverter erpToKingdeeConverter;

    /**
     * @Author Wqh
     * @Description 上传eccang产品信息
     * @Date 11:18 2024/11/5
     * @Param [message]
     * @return void
     **/
    @ServiceActivator(inputChannel = "customRuleChannel")
    public void syncCustomRuleToEccang(Message<List<ErpCustomRuleDTO>> message) {
        log.info("syncCustomRuleToEccang");
        List<EccangProduct> eccangProducts = erpToEccangConverter.customRuleDTOToProduct(message.getPayload());
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
            //用product_id在供应商产品里面查，使用查到的第一个价格
            // 1. 设置默认值
            eccangProduct.setCurrencyCode(
                ObjectUtils.defaultIfNull(eccangProduct.getCurrencyCode(), "1") // 默认 CNY
            );
            eccangProduct.setProductPrice(
                ObjectUtils.defaultIfNull(eccangProduct.getProductPrice(), 0f) // 默认价格为 0.0
            );

            // 2. 获取产品并处理价格
            erpSupplierProductService.getSupplierProductPage(
                    new ErpSupplierProductPageReqVO().setProductId(Long.valueOf(eccangProduct.getDesc()))
                )
                .getList().stream()
                .findFirst()
                .ifPresent(erpSupplierProductDO -> {
                    // 设置货币单位
                    Optional.ofNullable(erpSupplierProductDO.getPurchasePriceCurrencyCode())
                        .map(String::valueOf) // 将 Integer 转换为字符串
                        .filter(StringUtils::isNotBlank)
                        .ifPresent(eccangProduct::setCurrencyCode);

                    // 设置价格，并确保价格为 BigDecimal 类型，避免转换不一致
                    Optional.ofNullable(erpSupplierProductDO.getPurchasePrice())
                        .map(BigDecimal::valueOf)
                        .map(price -> price.setScale(2, RoundingMode.HALF_UP).floatValue())
                        .ifPresent(eccangProduct::setProductPrice);
                });
        }
        eccangService.addBatchProduct(eccangProducts);
        log.info("syncCustomRuleToEccang end countSize{{}}",eccangProducts.size());
    }

    /**
     * @Author Wqh
     * @Description 上传金蝶产品信息
     * @Date 11:18 2024/11/5
     * @Param [message]
     * @return void
     **/
    @ServiceActivator(inputChannel = "customRuleChannel")
    public void syncCustomRuleToKingdee(Message<List<ErpCustomRuleDTO>> message) {
        log.info("syncCustomRuleToKingdee");
        List<KingdeeProduct> kingdee = erpToKingdeeConverter.customRuleDTOToProduct(message.getPayload());
        for (KingdeeProduct kingdeeProduct : kingdee){
            kingdeeService.addProduct(kingdeeProduct);
        }
        log.info("syncCustomRuleToKingdee end");
    }

}
