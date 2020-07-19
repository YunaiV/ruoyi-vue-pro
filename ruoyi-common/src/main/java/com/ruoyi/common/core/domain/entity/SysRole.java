package com.ruoyi.common.core.domain.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 角色表 sys_role
 * 
 * @author ruoyi
 */
public class SysRole extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Excel(name = "角色序号", cellType = ColumnType.NUMERIC)
    private Long roleId;

    /** 角色名称 */
    @Excel(name = "角色名称")
    private String roleName;

    /** 角色权限 */
    @Excel(name = "角色权限")
    private String roleKey;

    /** 角色排序 */
    @Excel(name = "角色排序")
    private String roleSort;

    /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限） */
    @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限")
    private String dataScope;

    /** 角色状态（0正常 1停用） */
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 用户是否存在此角色标识 默认不存在 */
    private boolean flag = false;

    /** 菜单组 */
    private Long[] menuIds;

    /** 部门组（数据权限） */
    private Long[] deptIds;

    public SysRole()
    {

    }

    public SysRole(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    @NotBlank(message = "权限字符不能为空")
    @Size(min = 0, max = 100, message = "权限字符长度不能超过100个字符")
    public String getRoleKey()
    {
        return roleKey;
    }

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    @NotBlank(message = "显示顺序不能为空")
    public String getRoleSort()
    {
        return roleSort;
    }

    public void setRoleSort(String roleSort)
    {
        this.roleSort = roleSort;
    }

    public String getDataScope()
    {
        return dataScope;
    }

    public void setDataScope(String dataScope)
    {
        this.dataScope = dataScope;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public boolean isFlag()
    {
        return flag;
    }

    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }

    public Long[] getMenuIds()
    {
        return menuIds;
    }

    public void setMenuIds(Long[] menuIds)
    {
        this.menuIds = menuIds;
    }

    public Long[] getDeptIds()
    {
        return deptIds;
    }

    public void setDeptIds(Long[] deptIds)
    {
        this.deptIds = deptIds;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roleId", getRoleId())
            .append("roleName", getRoleName())
            .append("roleKey", getRoleKey())
            .append("roleSort", getRoleSort())
            .append("dataScope", getDataScope())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
