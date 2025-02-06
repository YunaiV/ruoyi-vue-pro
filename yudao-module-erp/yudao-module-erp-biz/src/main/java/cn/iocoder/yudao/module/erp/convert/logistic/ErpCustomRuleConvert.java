package cn.iocoder.yudao.module.erp.convert.logistic;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.convert.product.ErpProductConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface ErpCustomRuleConvert {
    ErpCustomRuleConvert INSTANCE = Mappers.getMapper(ErpCustomRuleConvert.class);
    //包含产品的 详情规则 转换
    default ErpCustomRuleDTO convert(ErpCustomRuleDO erpCustomRuleDO, ErpProductDO erpProductDO) {
        return BeanUtils.toBean(erpCustomRuleDO, ErpCustomRuleDTO.class).setProductDTO(ErpProductConvert.INSTANCE.convert(erpProductDO));
    }
    //包含产品的 详情规则 转换集合
    default List<ErpCustomRuleDTO> convert(List<ErpCustomRuleDO> erpCustomRuleDOs, Map<Long, ErpProductDO> productMap) {
        return erpCustomRuleDOs.stream()
            .filter(Objects::nonNull)
            .map(erpCustomRuleDO -> {
                ErpProductDO productDO = productMap.get(erpCustomRuleDO.getProductId());
                return ErpCustomRuleConvert.INSTANCE.convert(erpCustomRuleDO, productDO);
            })
            .toList();
    }
}


