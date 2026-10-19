package cn.iocoder.yudao.module.oa.dal.dataobject.regular;

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

import java.time.LocalDateTime;

/**
 * OA 转正申请 DO
 *
 * 申请人使用 {@link BaseDO#getCreator()}，申请时间使用 {@link BaseDO#getCreateTime()}
 *
 * @author 芋道源码
 */
@TableName("oa_regular_apply")
@KeySequence("oa_regular_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaRegularApplyDO extends BaseDO {

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
     * 试用开始时间
     */
    private LocalDateTime startTime;
    /**
     * 试用结束时间
     */
    private LocalDateTime endTime;
    /**
     * 试用天数
     *
     * 根据 {@link #startTime} 和 {@link #endTime} 计算，不重复保存流程公共天数
     */
    private Integer days;
    /**
     * 试用期或实习期心得体会
     */
    private String experience;
    /**
     * 对本岗位职责要求的理解
     */
    private String understanding;
    /**
     * 试用期或实习期的成长
     */
    private String growth;
    /**
     * 目前存在的不足
     */
    private String deficiency;
    /**
     * 本岗位工作改进计划
     */
    private String improvement;
    /**
     * 对公司产品的意见及建议
     */
    private String suggestion;
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
