package cn.iocoder.yudao.module.oa.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskStatusEnum;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * OA 任务 DO
 *
 * @author 芋道源码
 */
@TableName("oa_task")
@KeySequence("oa_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaTaskDO extends BaseDO {

    /**
     * 任务编号
     */
    @TableId
    private Long id;
    /**
     * 任务类型
     *
     * 枚举 {@link OaTaskTypeEnum}
     */
    private Integer type;
    /**
     * 总体状态
     *
     * 枚举 {@link OaTaskStatusEnum}
     */
    private Integer status;
    /**
     * 标题
     */
    private String title;
    /**
     * 任务描述
     */
    private String description;
    /**
     * 任务评价
     */
    private String comment;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 业务发布时间，新增及编辑任务时刷新
     */
    private LocalDateTime publishTime;
    /**
     * 是否置顶
     */
    private Boolean top;
    /**
     * 是否取消
     *
     * 取消后禁止反馈；恢复时保留原任务进度，不修改 status 或接收人状态。
     */
    private Boolean canceled;

}
