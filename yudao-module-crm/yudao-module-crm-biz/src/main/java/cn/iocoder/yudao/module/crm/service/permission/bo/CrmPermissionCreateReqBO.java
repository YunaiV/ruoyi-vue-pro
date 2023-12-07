package cn.iocoder.yudao.module.crm.service.permission.bo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * crm 数据权限 Create Req BO
 *
 * @author HUIHUI
 */
@Data
public class CrmPermissionCreateReqBO {

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
     * 权限级别
     */
    @NotNull(message = "权限级别不能为空")
    @InEnum(CrmPermissionLevelEnum.class)
    private Integer level;

}
