package cn.iocoder.yudao.module.tms.api.logistic;

import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.TmsCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import cn.iocoder.yudao.module.tms.convert.logistic.TmsCustomRuleConvert;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule.TmsCustomRuleMapper;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.TmsCustomRuleService;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.TmsCustomRuleBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.ErrorCodeConstants.CUSTOM_RULE_NOT_EXISTS;

@Service
public class TmsCustomRuleApiImpl implements TmsCustomRuleApi {
    //    @Autowired
//    ErpProductMapper tmsProductMapper;
    @Autowired
    TmsCustomRuleMapper customRuleMapper;
    //    @Autowired
//    ErpProductService tmsProductService;
    @Autowired
    ErpProductApi erpProductApi;
    @Autowired
    TmsCustomRuleService tmsCustomRuleService;

    @Override
    public List<TmsCustomRuleDTO> listCustomRules(List<Long> ids) {
        // 获取 BO 列表
        List<TmsCustomRuleBO> boList = tmsCustomRuleService.getCustomRuleBOList(ids);
        if (boList.isEmpty()) {
            return List.of();
        }
        // 获取产品信息并转换
        List<Long> productIds = boList.stream()
            .map(TmsCustomRuleBO::getProductId)
            .distinct()  // 保证产品 ID 唯一
            .collect(Collectors.toList());

        Map<Long, ErpProductDTO> map = erpProductApi.getProductMap(productIds);
        return TmsCustomRuleConvert.INSTANCE.convert(boList, map);
    }


    @Override
    public TmsCustomRuleDTO getErpCustomRule(Long id) {
        //1.0 根据海关规则的id获得产品id
        TmsCustomRuleBO ruleBO = tmsCustomRuleService.getCustomRuleBOById(id);
        if (ruleBO == null) {
            throw exception(CUSTOM_RULE_NOT_EXISTS, id);
        }
        //2.0 获得产品
        ErpProductDTO productDto = erpProductApi.getProductDto(ruleBO.getProductId());
        return TmsCustomRuleConvert.INSTANCE.convert(ruleBO, productDto);
    }


    @Override
    public List<TmsCustomRuleDTO> listDTOsByProductId(Long productId) {
        //获取规则和产品信息
        List<TmsCustomRuleBO> ruleBOS = customRuleMapper.selectByProductId(List.of(productId));
        if (ruleBOS.isEmpty()) {
            return null;
        }
        Map<Long, ErpProductDTO> map = erpProductApi.getProductMap(List.of(productId));
        return TmsCustomRuleConvert.INSTANCE.convert(ruleBOS, map);
    }
}
