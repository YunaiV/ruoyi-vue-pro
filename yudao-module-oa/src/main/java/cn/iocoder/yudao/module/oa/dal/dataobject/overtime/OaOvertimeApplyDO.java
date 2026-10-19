package cn.iocoder.yudao.module.oa.dal.dataobject.overtime;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.flowable.engine.history.HistoricProcessInstance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OA 加班申请 DO
 *
 * 申请人使用 {@link BaseDO#getCreator()}，申请时间使用 {@link BaseDO#getCreateTime()}
 *
 * @author 芋道源码
 */
@TableName("oa_overtime_apply")
@KeySequence("oa_overtime_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaOvertimeApplyDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 申请标题
     */
    private String title;
    /**
     * 紧急程度
     *
     * 字典 {@link DictTypeConstants#APPLY_URGENCY}
     */
    private Integer urgency;
    /**
     * 加班类型
     *
     * 字典 {@link DictTypeConstants#OVERTIME_TYPE}
     */
    private Integer type;
    /**
     * 加班开始时间
     */
    private LocalDateTime startTime;
    /**
     * 加班结束时间
     */
    private LocalDateTime endTime;
    /**
     * 加班天数
     */
    private BigDecimal days;
    /**
     * 申请原因
     */
    private String reason;
    /**
     * 审批状态，未开始表示尚未提交的草稿
     *
     * 枚举 {@link BpmProcessInstanceStatusEnum}
     */
    private Integer status;
    /**
     * BPM 流程实例编号
     *
     * 关联 {@link HistoricProcessInstance#getId()}
     */
    private String processInstanceId;

}
