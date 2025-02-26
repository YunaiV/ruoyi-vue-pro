package cn.iocoder.yudao.module.erp.api.logistic;

import cn.iocoder.yudao.module.erp.api.logistic.customrule.ErpCustomRuleApi;
import cn.iocoder.yudao.module.erp.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.convert.logistic.ErpCustomRuleConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.service.logistic.customrule.ErpCustomRuleService;
import cn.iocoder.yudao.module.erp.service.logistic.customrule.bo.ErpCustomRuleBO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_NOT_EXISTS;

@Service
public class ErpCustomRuleApiImpl implements ErpCustomRuleApi {
    @Autowired
    ErpProductMapper erpProductMapper;
    @Autowired
    ErpCustomRuleMapper customRuleMapper;
    @Autowired
    ErpProductService erpProductService;
    @Autowired
    ErpCustomRuleService erpCustomRuleService;

    @Override
    public List<ErpCustomRuleDTO> listCustomRules(List<Long> ids) {
        // 获取 BO 列表
        List<ErpCustomRuleBO> boList = erpCustomRuleService.getCustomRuleBOList(ids);
        if (boList.isEmpty()) {
            return List.of();
        }
        // 获取产品信息并转换
        List<Long> productIds = boList.stream()
            .map(ErpCustomRuleBO::getProductId)
            .distinct()  // 保证产品 ID 唯一
            .collect(Collectors.toList());

        Map<Long, ErpProductDO> productMap = erpProductService.getProductMap(productIds);
        return ErpCustomRuleConvert.INSTANCE.convert(boList, productMap);
    }


    @Override
    public ErpCustomRuleDTO getErpCustomRule(Long id) {
        //1.0 根据海关规则的id获得产品id
        ErpCustomRuleBO ruleBO = erpCustomRuleService.getCustomRuleBOById(id);
        if (ruleBO == null) {
            throw exception(CUSTOM_RULE_NOT_EXISTS, id);
        }
        //2.0 获得产品
        ErpProductDO erpProductDO = erpProductMapper.selectById(ruleBO.getProductId());
        return ErpCustomRuleConvert.INSTANCE.convert(ruleBO, erpProductDO);
    }


    @Override
    public List<ErpCustomRuleDTO> listDTOsByProductId(Long productId) {
        //获取规则和产品信息
        List<ErpCustomRuleBO> ruleBOS = customRuleMapper.selectByProductId(List.of(productId));
        if (ruleBOS.isEmpty()) {
            return null;
        }
        Map<Long, ErpProductDO> productMap = erpProductService.getProductMap(List.of(productId));
        return ErpCustomRuleConvert.INSTANCE.convert(ruleBOS, productMap);
    }
}
