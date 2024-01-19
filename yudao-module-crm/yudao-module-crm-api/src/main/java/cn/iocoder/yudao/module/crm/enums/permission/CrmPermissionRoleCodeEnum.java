package cn.iocoder.yudao.module.crm.enums.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Crm 数据权限角色枚举
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum CrmPermissionRoleCodeEnum {

    CRM_ADMIN("crm_admin", "CRM 管理员");

    /**
     * 角色标识
     */
    private String code;
    /**
     * 角色名称
     */
    private String name;

}

