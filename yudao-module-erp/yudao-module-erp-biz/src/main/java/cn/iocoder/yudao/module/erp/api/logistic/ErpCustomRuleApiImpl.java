package cn.iocoder.yudao.module.erp.api.logistic;

import cn.iocoder.yudao.module.erp.api.logistic.customrule.ErpCustomRuleApi;
import cn.iocoder.yudao.module.erp.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.convert.logistic.ErpCustomRuleConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_NOT_EXISTS;

@Service
public class ErpCustomRuleApiImpl implements ErpCustomRuleApi {
    @Autowired
    ErpProductMapper erpProductMapper;
    @Resource
    ErpCustomRuleMapper customRuleMapper;
    @Resource
    ErpProductService erpProductService;

    /**
     * 获得所有海关规则列表
     *
     * @param ids 如果为null则返回所有
     * @return List<ErpProductDetailDTO>
     */
    @Override
    public List<ErpCustomRuleDTO> listCustomRules(List<Long> ids) {
        List<Long> productDOIds = ids == null ? erpProductMapper.selectList().stream().map(ErpProductDO::getId).toList() : ids;//TODO 后续：关闭状态的产品，不同步。
        Map<Long, ErpProductDO> productMap = erpProductService.getProductMap(productDOIds);
        List<ErpCustomRuleDO> erpCustomRules = customRuleMapper.selectByProductId(productDOIds);
        return ErpCustomRuleConvert.INSTANCE.convert(erpCustomRules, productMap);
    }

    /**
     * 根据海关规则id获取产品的全量信息（海关规则，产品供应商）
     *
     * @param id 海关规则id
     * @return List<ErpCustomRuleDetailDTO> DTO
     */
    @Override
    public ErpCustomRuleDTO getErpCustomRuleDTOById(Long id) {
        validateCustomRuleExists(id);
        //1.0 根据海关规则的id获得产品id
        ErpCustomRuleDO erpCustomRuleDO = customRuleMapper.selectById(id);
        //2.0 获得产品
        ErpProductDO erpProductDO = erpProductMapper.selectById(erpCustomRuleDO.getProductId());
        return ErpCustomRuleConvert.INSTANCE.convert(erpCustomRuleDO, erpProductDO);
    }

    private void validateCustomRuleExists(Long id) {
        if (customRuleMapper.selectById(id) == null) {
            throw exception(CUSTOM_RULE_NOT_EXISTS);
        }
    }

    /**
     * 根绝1个产品id(确保存在)获取n个产品DTO，获取产品+海关规则。如果海关规则无匹配，则返回null
     *
     * @param productId 产品id
     * @return List<ErpCustomRuleDTO> 海关规则+产品 DTOs
     */
    @Override
    public List<ErpCustomRuleDTO> getErpCustomRuleDTOByProductId(Long productId) {
        List<ErpCustomRuleDO> ruleDOS = customRuleMapper.selectByProductId(List.of(productId));
        Map<Long, ErpProductDO> productMap = erpProductService.getProductMap(List.of(productId));
        if (!ruleDOS.isEmpty()) {
            return ErpCustomRuleConvert.INSTANCE.convert(ruleDOS, productMap);
        }
        return null;
    }
}
