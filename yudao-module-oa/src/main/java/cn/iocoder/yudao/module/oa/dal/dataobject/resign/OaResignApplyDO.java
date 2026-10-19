package cn.iocoder.yudao.module.oa.dal.dataobject.resign;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.flowable.engine.history.HistoricProcessInstance;

/**
 * OA 离职申请 DO
 *
 * 申请人使用 {@link BaseDO#getCreator()}，申请时间使用 {@link BaseDO#getCreateTime()}
 *
 * @author 芋道源码
 */
@TableName("oa_resign_apply")
@KeySequence("oa_resign_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaResignApplyDO extends BaseDO {

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
     * 申请原因
     */
    private String reason;
    /**
     * 工作交接人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long handoverUserId;
    /**
     * 未完成事宜
     */
    private String unfinishedWork;
    /**
     * 是否有未完成的费用报销
     *
     * 勾选表示存在未完成报销，不表示报销已经完成
     */
    private Boolean hasPendingReimbursement;
    /**
     * 申请人的意见及建议
     *
     * 保留申请数据，是否展示输入入口由表单评审确定
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
