package cn.iocoder.yudao.module.tms.api.logistic.customrule;

import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

//@FeignClient(name = "tms-custom", path = "/tms/custom")
@Validated
public interface TmsCustomRuleApi {

    /**
     * 获取所有海关规则(含产品信息)DTO
     *
     * @param ids 海关规则id集合,为空则返回所有
     * @return 海关规则DTO集合
     */
    List<TmsCustomRuleDTO> listCustomRules(List<Long> ids);

    /**
     * 根据海关规则id 获得DTO（含海关的产品）
     *
     * @param id 海关规则id
     * @return TmsCustomRuleDTO dto
     */
    TmsCustomRuleDTO getErpCustomRule(@NotNull Long id);

    /**
     * 根绝产品id(确保存在)获取n个产品DTO，获取产品+海关规则。如果海关规则无匹配，则返回null
     * <p>
     *
     * @param productId 产品id
     * @return List<TmsCustomRuleDTO> 海关规则DTO +产品 DTOs
     */
    List<TmsCustomRuleDTO> listCustomRuleDTOsByProductId(@NotNull Long productId);
}
