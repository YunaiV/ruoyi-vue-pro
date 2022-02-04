package cn.iocoder.yudao.module.system.dal.dataobject.permission;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.JsonLongSetTypeHandler;
import cn.iocoder.yudao.framework.security.core.enums.DataScopeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 角色 DO
 *
 * @author ruoyi
 */
@TableName(value = "system_role", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleDO extends BaseDO {

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
     * 枚举
     */
    private String code;
    /**
     * 角色排序
     */
    private Integer sort;
    /**
     * 角色状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 角色类型
     *
     * 枚举
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;

    /**
     * 数据范围
     *
     * 枚举 {@link DataScopeEnum}
     */
    private Integer dataScope;
    /**
     * 数据范围(指定部门数组)
     *
     * 适用于 {@link #dataScope} 的值为 {@link DataScopeEnum#DEPT_CUSTOM} 时
     */
    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> dataScopeDeptIds;

}
