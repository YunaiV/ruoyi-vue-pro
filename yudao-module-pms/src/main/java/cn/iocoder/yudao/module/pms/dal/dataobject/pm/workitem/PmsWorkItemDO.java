package cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemDefectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemLifecycleStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PMS 工作项 DO
 *
 * @author 芋道源码
 */
@TableName(value = "pms_work_item", autoResultMap = true)
@KeySequence("pms_work_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsWorkItemDO extends BaseDO {

    /**
     * 工作项编号
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
     * 工作项类型
     *
     * 枚举 {@link PmsWorkItemTypeEnum}
     */
    private Integer type;
    /**
     * 项目内工作项序号
     */
    private Integer serialNumber;
    /**
     * 工作项标题
     */
    private String name;
    /**
     * 工作项描述
     */
    private String description;
    /**
     * 优先级
     *
     * 枚举 {@link PmsWorkItemPriorityEnum}
     */
    private Integer priority;
    /**
     * 负责人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long assigneeUserId;
    /**
     * 看板状态编号
     *
     * 关联 {@link PmsWorkItemStatusDO#getId()}
     */
    private Long statusId;
    /**
     * 语义状态
     *
     * 枚举 {@link PmsWorkItemStatusTypeEnum}
     */
    private Integer status;
    /**
     * 生命周期状态
     *
     * 枚举 {@link PmsWorkItemLifecycleStatusEnum}
     */
    private Integer lifecycleStatus;
    /**
     * 归档时间
     */
    private LocalDateTime archiveTime;
    /**
     * 移入回收站时间
     */
    private LocalDateTime recycleTime;
    /**
     * 所属迭代编号
     *
     * 关联 {@link PmsIterationDO#getId()}
     */
    private Long iterationId;
    /**
     * 父工作项编号
     *
     * 关联 {@link #getId()}
     */
    private Long parentId;
    /**
     * 关联需求编号
     *
     * 关联 {@link #getId()}
     */
    private Long relatedRequirementId;
    /**
     * 缺陷类型
     *
     * 枚举 {@link PmsWorkItemDefectTypeEnum}
     */
    private Integer defectType;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 截止时间
     */
    private LocalDateTime endTime;
    /**
     * 预估工时，单位：小时
     */
    private Integer estimatedHours;
    /**
     * 完成进度，取值范围 0-100
     */
    private Integer progress;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;
    /**
     * 标签编号列表
     *
     * 关联 {@link PmsWorkItemLabelDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> labelIds;
    /**
     * 看板内显示顺序
     */
    private Integer sort;

}
