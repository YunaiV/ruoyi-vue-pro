package cn.iocoder.yudao.module.pms.dal.dataobject.pm.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectGroupTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 项目分组 DO
 *
 * 项目分组是后台用户个人维护的项目视图，不影响其他成员看到的项目分组
 *
 * @author 芋道源码
 */
@TableName("pms_project_group")
@KeySequence("pms_project_group_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsProjectGroupDO extends BaseDO {

    /**
     * 分组编号
     */
    @TableId
    private Long id;
    /**
     * 后台用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 分组名称
     */
    private String name;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 分组类型
     *
     * 枚举 {@link PmsProjectGroupTypeEnum}
     */
    private Integer type;

}
