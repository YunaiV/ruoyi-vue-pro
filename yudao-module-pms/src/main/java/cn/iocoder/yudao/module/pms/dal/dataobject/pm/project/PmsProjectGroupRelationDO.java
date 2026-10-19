package cn.iocoder.yudao.module.pms.dal.dataobject.pm.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 项目分组关系 DO
 *
 * @author 芋道源码
 */
@TableName("pms_project_group_relation")
@KeySequence("pms_project_group_relation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsProjectGroupRelationDO extends BaseDO {

    /**
     * 编号
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
     * 项目分组编号
     *
     * 关联 {@link PmsProjectGroupDO#getId()}
     */
    private Long groupId;
    /**
     * 项目编号
     *
     * 关联 {@link PmsProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 显示顺序
     */
    private Integer sort;

}
