package cn.iocoder.yudao.module.crm.service.permission.bo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 数据权限转移 Request BO
 *
 * @author HUIHUI
 */
@Data
public class CrmTransferPermissionReqBO {

    /**
     * 当前登录用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * Crm 类型
     */
    @NotNull(message = "Crm 类型不能为空")
    @InEnum(CrmBizTypeEnum.class)
    private Integer bizType;

    /**
     * 数据编号
     */
    @NotNull(message = "Crm 数据编号不能为空")
    private Long bizId;

    /**
     * 新负责人的用户编号
     */
    @NotNull(message = "新负责人的用户编号不能为空")
    private Long newOwnerUserId;

    /**
     * 老负责人是否加入团队，是/否
     */
    @NotNull(message = "老负责人是否加入团队不能为空")
    private Boolean joinTeam;

    /**
     * 老负责人加入团队后的权限级别。如果 {@link #joinTeam} 为 false, permissionLevel 为 null
     * 关联 {@link CrmPermissionLevelEnum}
     */
    private Integer permissionLevel;

}
