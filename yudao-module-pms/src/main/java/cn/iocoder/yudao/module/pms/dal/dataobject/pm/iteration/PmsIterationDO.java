package cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.enums.pm.iteration.PmsIterationStatusEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * PMS 项目迭代 DO
 *
 * @author 芋道源码
 */
@TableName("pms_iteration")
@KeySequence("pms_iteration_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsIterationDO extends BaseDO {

    /**
     * 迭代编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     *
     * 关联 {@link PmsProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 迭代名称
     */
    private String name;
    /**
     * 负责人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long ownerUserId;
    /**
     * 迭代状态
     *
     * 枚举 {@link PmsIterationStatusEnum}
     */
    private Integer status;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    /**
     * 迭代目标
     */
    private String target;
    /**
     * 迭代描述
     */
    private String description;
    /**
     * 显示顺序
     */
    private Integer sort;

}
