package cn.iocoder.yudao.module.erp.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_PART_NULL;
import static cn.iocoder.yudao.module.erp.util.ConstantConvertUtils.getProductStatus;

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
    private final DeptApi deptApi;
    private final AdminUserApi userApi;

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
                .anyMatch(i -> StrUtil.isBlank(i.getName()) || StrUtil.isBlank(i.getSupplierProductCode())), CUSTOM_RULE_PART_NULL);
        //获取产品部门信息
        Map<Long, DeptRespDTO> productDeptMap = deptApi.getDeptMap(convertSet(erpCustomRuleDtos, ErpCustomRuleDTO::getProductDeptId));
        //获取创建人用户信息
        Map<Long, AdminUserRespDTO> userMap = userApi.getUserMap(convertSet(erpCustomRuleDtos, i -> Long.parseLong(i.getCreator())));
        //获取创建人部门信息
        Map<Long, DeptRespDTO> creatorDeptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        List<ErpCustomRuleDTO> productDtos = BeanUtils.toBean(erpCustomRuleDtos, ErpCustomRuleDTO.class, product -> {
            //设置产品部门名称
            MapUtils.findAndThen(productDeptMap, product.getProductDeptId(),
                    dept -> product.setProductDeptName(dept.getName()));
            //设置产品创建人部门名称
            MapUtils.findAndThen(userMap, Long.parseLong(product.getCreator()),
                    user -> MapUtils.findAndThen(creatorDeptMap, user.getDeptId(),
                            dept -> product.setCreatorDeptName(dept.getName())));
            //如果有供应商产品编码和国家代码都不为空的时候才去设置SKU
            if (StrUtil.isNotBlank(product.getSupplierProductCode()) && StrUtil.isNotBlank(product.getCountryCode())) {
                product.setProductSku(product.getSupplierProductCode() + "-" + getProductStatus(product.getCountryCode()));
            }
        });
        //同步产品信息
        if (CollUtil.isNotEmpty(productDtos)){
            productChannel.send(MessageBuilder.withPayload(productDtos).build());
        }
    }
}
