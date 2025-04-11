package com.somle.esb.handler.oms;

import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.oms.api.dto.SkuRelationDTO;
import com.somle.eccang.model.req.oms.EccangModifySkuRelationReqVO;
import com.somle.eccang.service.EccangService;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: OMS同步SKU映射关系到易仓
 * @Author: gumaomao
 * @Date: 2025/4/10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EccangSkuRelationHandler {

    @Resource
    EccangService service;

    @ServiceActivator(inputChannel = "eccangSkuRelationOutputChannel")
    public void handleSale(SkuRelationDTO relationDTO) {

        if (SpringUtils.isProd()) {
            return;
        }

        if (relationDTO == null) {
            return;
        }

        if (StringUtil.isBlank(relationDTO.getPlatformSku())) {
            return;
        }

        if (StringUtil.isBlank(relationDTO.getShopName())) {
            return;
        }

        List<EccangModifySkuRelationReqVO.PCR> list = new ArrayList<>();
        for (SkuRelationDTO.Relation relation : relationDTO.getRelations()) {
            list.add(EccangModifySkuRelationReqVO.PCR.builder().productSku(relation.getProductSku()).productSkuQty(relation.getProductSkuQty() + "")
                .productSkuNameCn(relation.getProductSku()).build());
        }

        service.setSkuRelation(relationDTO.getPlatformSku(), relationDTO.getShopName(), list);

    }
}
