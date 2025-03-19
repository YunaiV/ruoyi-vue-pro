package com.somle.esb.handler;

import cn.iocoder.yudao.framework.common.enums.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.ErpCustomRuleDTO;
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
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/13$
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ErpCustomRuleHandler {

    private final KingdeeService kingdeeService;
    private final EccangService eccangService;
    //    private final ErpSupplierProductService erpSupplierProductService;
    private final ErpToEccangConverter erpToEccangConverter;
    private final ErpToKingdeeConverter erpToKingdeeConverter;
    private final DictDataApi dictDataApi;

    /**
     * @return void
     * @Author Wqh
     * @Description 上传eccang产品信息
     * @Date 11:18 2024/11/5
     * @Param [message]
     **/
    @ServiceActivator(inputChannel = "erpCustomRuleChannel")
    public void syncCustomRulesToEccang(@Payload List<ErpCustomRuleDTO> customRules) {
        List<EccangProduct> eccangProducts = erpToEccangConverter.convertByErpCustomDTOs(processRules(customRules));
        eccangProducts.forEach(eccangProduct -> eccangService.addBatchProduct(List.of(eccangProduct)));
        log.info("syncCustomRuleToEccang end ,sku={{}}", eccangProducts.stream().map(EccangProduct::getProductSku).toList());
    }

    /**
     * @return void
     * @Author Wqh
     * @Description 上传金蝶产品信息
     * @Date 11:18 2024/11/5
     * @Param [message]
     **/
    @ServiceActivator(inputChannel = "erpCustomRuleChannel")
    public void syncCustomRulesToKingdee(@Payload List<ErpCustomRuleDTO> customRules) {
        List<KingdeeProductSaveReqVO> kingdee = erpToKingdeeConverter.convert(processRules(customRules));
        kingdee.forEach(kingdeeService::addProduct);
        log.info("syncCustomRuleToKingdee end,skus={{}}}", kingdee.stream().map(KingdeeProductSaveReqVO::getNumber).toList());
    }


    /**
     * 复制 countryCode 为 CN 字典映射值的对象，添加到list中。相当于含有CN的规则，那么list就多一个无国别规则。
     *
     * @param customRules 原始海关规则列表
     * @return 处理后的海关规则列表 List<ErpCustomRuleDTO>
     */
    private List<ErpCustomRuleDTO> processRules(List<ErpCustomRuleDTO> customRules) {
        CopyOnWriteArrayList<ErpCustomRuleDTO> processedRules = new CopyOnWriteArrayList<>(customRules);
        customRules.stream()
            .filter(customRule -> customRule.getCountryCode() != null)
            .forEach(customRule -> Optional.ofNullable(dictDataApi.parseDictData(DictTypeConstants.COUNTRY_CODE, "CN"))
                .flatMap(dictDataRespDTO -> Optional.ofNullable(dictDataRespDTO.getValue()))
                .ifPresent(value -> {
                    Integer countryCode = Integer.valueOf(value);
                    if (customRule.getCountryCode().equals(countryCode)) {
                        //当前存在国家是CN的数据
                        ErpCustomRuleDTO bean = BeanUtils.toBean(customRule, ErpCustomRuleDTO.class);
                        processedRules.add(BeanUtils.toBean(bean.setCountryCode(null), ErpCustomRuleDTO.class));
                    }
                }));
        return processedRules;
    }
}
