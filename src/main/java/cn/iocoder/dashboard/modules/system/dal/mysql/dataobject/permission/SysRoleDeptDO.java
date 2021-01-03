package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission;

import cn.iocoder.dashboard.framework.mybatis.core.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门和角色关联 DO
 *
 * @author ruoyi
 */
@TableName("sys_role_dept")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleDeptDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 部门 ID
     */
    private Long deptId;
    /**
     * 角色 ID
     */
    private Long roleId;

}
