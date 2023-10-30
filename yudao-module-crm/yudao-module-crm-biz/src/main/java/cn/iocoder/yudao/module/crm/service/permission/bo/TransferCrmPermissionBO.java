package cn.iocoder.yudao.module.crm.service.permission.bo;

import cn.iocoder.yudao.module.crm.enums.common.PermissionTypeEnum;
import cn.iocoder.yudao.module.crm.enums.common.TransferTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

// TODO @puhui999：CrmTransferPermissionReqBO，一个是 Crm 前缀，一个 Req 表示入参
/**
 * 数据权限转移 BO
 *
 * @author HUIHUI
 */
@Data
public class TransferCrmPermissionBO {

    // TODO @puhui999：参数的注释
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    // TODO @puhui999：bizType
    /**
     * Crm 类型 关联 {@link CrmEnum} TODO 这种不用再写关联了，直接 @InEnum 参数校验
     */
    @NotNull(message = "Crm 类型不能为空")
    private Integer crmType;

    // TODO @puhui999：bizId
    /**
     * 数据编号
     */
    @NotNull(message = "Crm 数据编号不能为空")
    private Long crmDataId;

    // TODO @puhui999：要不这里改成 newOwnerUserId;然后，transferType 和 permissionType，合并成 oldOwnerPermission（空就是移除）
    @NotNull(message = "新负责人的用户编号不能为空")
    private Long ownerUserId;

    /**
     * 原负责人移除方式, 关联 {@link TransferTypeEnum} TODO 这种不用再写关联了，直接 @InEnum 参数校验
     */
    @NotNull(message = "原负责人移除方式不能为空")
    private Integer transferType;

    /**
     * 权限类型, 关联 {@link PermissionTypeEnum}
     */
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

}
