package cn.iocoder.yudao.module.tms.convert.logistic;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.TmsCustomRuleBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface TmsCustomRuleConvert {
    TmsCustomRuleConvert INSTANCE = Mappers.getMapper(TmsCustomRuleConvert.class);

    //包含产品的 详情规则 转换
    default TmsCustomRuleDTO convert(TmsCustomRuleBO ruleBO, ErpProductDTO dto) {
        return BeanUtils.toBean(ruleBO, TmsCustomRuleDTO.class).setProductDTO(dto);
    }

    //包含产品的 详情规则 转换集合
    default List<TmsCustomRuleDTO> convert(List<TmsCustomRuleBO> ruleBOList, Map<Long, ErpProductDTO> productMap) {
        return ruleBOList.stream()
            .filter(Objects::nonNull)
            .map(ruleDO -> {
                ErpProductDTO productDO = productMap.get(ruleDO.getProductId());
                return TmsCustomRuleConvert.INSTANCE.convert(ruleDO, productDO);
            })
            .toList();
    }
}


