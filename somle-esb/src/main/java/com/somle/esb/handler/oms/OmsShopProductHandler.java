package com.somle.esb.handler.oms;

import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductDTO;
import com.somle.eccang.model.req.oms.EccangModifySkuRelationReqVO;
import com.somle.eccang.service.EccangService;
import com.somle.esb.model.oms.dto.SkuRelationDTO;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: OMS同步sku映射关系数据到易仓
 * @Author: gumaomao
 * @Date: 2025/4/10
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class OmsShopProductHandler {

    @Resource
    EccangService service;

    @ServiceActivator(inputChannel = "omsShopProductOutPutChannel")
    public void handleModifySkuRelation(OmsShopProductDTO productDTO) {

        if (productDTO == null) {
            return;
        }

        if (StringUtil.isBlank(productDTO.getCode())) {
            return;
        }

        if (StringUtil.isBlank(productDTO.getShop().getName())) {
            return;
        }

        SkuRelationDTO relationDTO = SkuRelationDTO.builder()
            .platformSku(productDTO.getCode())
            .shopName(productDTO.getShop().getName())
            .relations(StreamX.from(productDTO.getItems()).map(item -> SkuRelationDTO.Relation.builder()
                // 如果是非生产环境加 TEST- 前缀区别
                .productSku(SpringUtils.isProd() ? "" : "TEST-" + item.getProduct().getBarCode())
                .productSkuQty(item.getQty())
                .build()).toList())
            .build();


        List<EccangModifySkuRelationReqVO.PCR> list = new ArrayList<>();
        for (SkuRelationDTO.Relation relation : relationDTO.getRelations()) {
            list.add(EccangModifySkuRelationReqVO.PCR.builder().productSku(relation.getProductSku()).productSkuQty(relation.getProductSkuQty() + "")
                .productSkuNameCn(relation.getProductSku()).build());
        }
        service.modifySkuRelation(relationDTO.getPlatformSku(), relationDTO.getShopName(), list);
    }
}
