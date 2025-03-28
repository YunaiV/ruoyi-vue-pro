package cn.iocoder.yudao.module.tms.api.logistic;

import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.ErpCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.tms.convert.logistic.ErpCustomRuleConvert;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.ErpCustomRuleService;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.ErpCustomRuleBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.ErrorCodeConstants.CUSTOM_RULE_NOT_EXISTS;

@Service
public class ErpCustomRuleApiImpl implements ErpCustomRuleApi {
    //    @Autowired
//    ErpProductMapper tmsProductMapper;
    @Autowired
    ErpCustomRuleMapper customRuleMapper;
    //    @Autowired
//    ErpProductService tmsProductService;
    @Autowired
    ErpProductApi erpProductApi;
    @Autowired
    ErpCustomRuleService tmsCustomRuleService;

    @Override
    public List<ErpCustomRuleDTO> listCustomRulesByIds(List<Long> ids) {
        // 获取 BO 列表
        List<ErpCustomRuleBO> boList = tmsCustomRuleService.getCustomRuleBOList(ids);
        if (boList.isEmpty()) {
            return List.of();
        }
        // 获取产品信息并转换
        List<Long> productIds = boList.stream()
            .map(ErpCustomRuleBO::getProductId)
            .distinct()  // 保证产品 ID 唯一
            .collect(Collectors.toList());

        Map<Long, ErpProductDTO> map = erpProductApi.getProductMap(productIds);
        return ErpCustomRuleConvert.INSTANCE.convert(boList, map);
    }


    @Override
    public ErpCustomRuleDTO getErpCustomRule(Long id) {
        //1.0 根据海关规则的id获得产品id
        ErpCustomRuleBO ruleBO = tmsCustomRuleService.getCustomRuleBOById(id);
        if (ruleBO == null) {
            throw exception(CUSTOM_RULE_NOT_EXISTS, id);
        }
        //2.0 获得产品
        ErpProductDTO productDto = erpProductApi.getProductDto(ruleBO.getProductId());
        return ErpCustomRuleConvert.INSTANCE.convert(ruleBO, productDto);
    }


    @Override
    public List<ErpCustomRuleDTO> listDTOsByProductId(Long productId) {
        //获取规则和产品信息
        List<ErpCustomRuleBO> ruleBOS = customRuleMapper.selectByProductId(List.of(productId));
        //根据产品id->海关分类BO
        if (ruleBOS.isEmpty()) {
            return null;
        }
        Map<Long, ErpProductDTO> map = erpProductApi.getProductMap(List.of(productId));
        return ErpCustomRuleConvert.INSTANCE.convert(ruleBOS, map);
    }
}
