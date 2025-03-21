package cn.iocoder.yudao.module.tms.convert.logistic;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.ErpCustomRuleBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface ErpCustomRuleConvert {
    ErpCustomRuleConvert INSTANCE = Mappers.getMapper(ErpCustomRuleConvert.class);

    //包含产品的 详情规则 转换
    default ErpCustomRuleDTO convert(ErpCustomRuleBO ruleBO, ErpProductDTO dto) {
        return BeanUtils.toBean(ruleBO, ErpCustomRuleDTO.class).setProductDTO(dto);
    }

    //包含产品的 详情规则 转换集合
    default List<ErpCustomRuleDTO> convert(List<ErpCustomRuleBO> ruleBOList, Map<Long, ErpProductDTO> productMap) {
        return ruleBOList.stream()
            .filter(Objects::nonNull)
            .map(ruleDO -> {
                ErpProductDTO productDO = productMap.get(ruleDO.getProductId());
                return ErpCustomRuleConvert.INSTANCE.convert(ruleDO, productDO);
            })
            .toList();
    }
}


