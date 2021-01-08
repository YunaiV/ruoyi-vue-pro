package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.framework.security.core.enums.DataScopeEnum;
import cn.iocoder.dashboard.modules.system.enums.permission.RoleCodeEnum;
import cn.iocoder.dashboard.modules.system.enums.permission.RoleTypeEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色 DO
 *
 * @author ruoyi
 */
@TableName("sys_role")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleDO extends BaseDO {

    /**
     * 角色ID
     */
    @TableId
    private Long id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色标识
     *
     * 枚举 {@link RoleCodeEnum}
     */
    private String code;
    /**
     * 角色排序
     */
    private Integer sort;
    /**
     * 数据范围
     *
     * 枚举类 {@link DataScopeEnum}
     */
    private Integer dataScope;
    /**
     * 角色状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private String status;
    /**
     * 角色类型
     *
     * 枚举 {@link RoleTypeEnum}
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     */
    private boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）
     */
    private boolean deptCheckStrictly;

}
