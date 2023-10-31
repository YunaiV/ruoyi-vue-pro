package cn.iocoder.yudao.module.crm.service.permission.bo;

import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

// TODO @puhui999：，一个是 Crm 前缀，一个 Req 表示入参
/**
 * crm 数据权限 Create BO
 *
 * @author HUIHUI
 */
@Data
public class CrmPermissionCreateBO {

    // TODO @puhui999：如果是关联字段，换一行写它的注释；不然看着略乱哈

    /**
     * Crm 类型 关联 {@link CrmBizTypeEnum}
     */
    @NotNull(message = "Crm 类型不能为空")
    private Integer crmType;
    /**
     * 数据编号 关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     */
    @NotNull(message = "Crm 数据编号不能为空")
    private Long crmDataId;

    /**
     * 负责人的用户编号 关联 AdminUser#id, null 则为公海数据
     */
    private Long ownerUserId;
    /**
     * 只读权限的用户编号数组
     */
    private Set<Long> roUserIds;
    /**
     * 读写权限的用户编号数组
     */
    private Set<Long> rwUserIds;

}
