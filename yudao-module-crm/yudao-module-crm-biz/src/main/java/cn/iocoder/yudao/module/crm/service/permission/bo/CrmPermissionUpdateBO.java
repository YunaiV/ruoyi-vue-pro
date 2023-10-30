package cn.iocoder.yudao.module.crm.service.permission.bo;

import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.framework.enums.CrmEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * crm 数据权限 Update BO
 *
 * @author HUIHUI
 */
@Data
public class CrmPermissionUpdateBO {

    /**
     * 数据权限编号 {@link CrmPermissionDO#getId()}
     */
    @NotNull(message = "Crm 数据权限编号不能为空")
    private Long id;

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
