package cn.iocoder.yudao.module.erp.api.logistic.customrule;

import cn.iocoder.yudao.module.erp.api.logistic.customrule.dto.ErpCustomRuleDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

//@FeignClient(name = "erp-custom", path = "/erp/custom")
public interface ErpCustomRuleApi {

    /**
     * 获取所有海关规则(含产品信息)DTO
     *
     * @param ids 海关规则id集合,为空则返回所有
     * @return 海关规则DTO集合
     */
    List<ErpCustomRuleDTO> listCustomRules(List<Long> ids);

    /**
     * 根据海关规则id 获得DTO（含海关的产品）
     *
     * @param id 海关规则id
     * @return ErpCustomRuleDTO dto
     */
    ErpCustomRuleDTO getErpCustomRule(@NotNull Long id);

    /**
     * 根绝产品id(确保存在)获取n个产品DTO，获取产品+海关规则。如果海关规则无匹配，则返回null
     * <p>
     *
     * @param productId 产品id
     * @return List<ErpCustomRuleDTO> 海关规则DTO +产品 DTOs
     */
    List<ErpCustomRuleDTO> listDTOsByProductId(@NotNull Long productId);
}
