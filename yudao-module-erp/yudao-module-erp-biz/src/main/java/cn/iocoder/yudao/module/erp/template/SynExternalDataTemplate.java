package cn.iocoder.yudao.module.erp.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.util.List;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_PART_NULL;

/**
 * @className: SynExternalDataTemplate
 * @author: Wqh
 * @date: 2024/11/11 14:35
 * @Version: 1.0
 * @description:
 */
@Component
@RequiredArgsConstructor
public class SynExternalDataTemplate {

    @Resource
    private MessageChannel productChannel;

    /**
    * @Author Wqh
    * @Description 同步产品数据
    * @Date 14:42 2024/11/11
    * @Param [erpProductDtos]
    * @return void
    **/
    public void synProductData(List<ErpCustomRuleDTO> erpCustomRuleDtos) {
        //判断数据是否存在不包含name和supplierProductCode
        //如果存在name或supplierProductCode为空的数据，则该数据一定是在海关规则管理模块中出现的非法数据，请检查海关规则模块
        ThrowUtil.ifThrow(CollUtil.isEmpty(erpCustomRuleDtos) || erpCustomRuleDtos
                .stream()
                .anyMatch(i -> StrUtil.isBlank(i.getProductName()) || StrUtil.isBlank(i.getSupplierProductCode())), CUSTOM_RULE_PART_NULL);
        //同步产品信息
        if (CollUtil.isNotEmpty(erpCustomRuleDtos)){
            productChannel.send(MessageBuilder.withPayload(erpCustomRuleDtos).build());
        }
    }
}
