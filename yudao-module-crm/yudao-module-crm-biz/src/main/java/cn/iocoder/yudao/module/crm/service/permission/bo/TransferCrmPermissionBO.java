package cn.iocoder.yudao.module.crm.service.permission.bo;

import cn.iocoder.yudao.module.crm.enums.common.PermissionTypeEnum;
import cn.iocoder.yudao.module.crm.enums.common.TransferTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 数据权限转移 BO
 *
 * @author HUIHUI
 */
@Data
public class TransferCrmPermissionBO {

    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * Crm 类型 关联 {@link CrmEnum}
     */
    @NotNull(message = "Crm 类型不能为空")
    private Integer crmType;

    /**
     * 数据编号 关联 {@link CrmEnum} 对应模块 DO#getId()
     */
    @NotNull(message = "Crm 数据编号不能为空")
    private Long crmDataId;

    @NotNull(message = "新负责人的用户编号不能为空")
    private Long ownerUserId;

    /**
     * 原负责人移除方式, 关联 {@link TransferTypeEnum}
     */
    @NotNull(message = "原负责人移除方式不能为空")
    private Integer transferType;

    /**
     * 权限类型, 关联 {@link PermissionTypeEnum}
     */
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

}
