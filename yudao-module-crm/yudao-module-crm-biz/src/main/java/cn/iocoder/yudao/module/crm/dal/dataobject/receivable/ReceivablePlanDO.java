package cn.iocoder.yudao.module.crm.dal.dataobject.receivable;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 回款计划 DO
 *
 * @author 芋道源码
 */
@TableName("crm_receivable_plan")
@KeySequence("crm_receivable_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivablePlanDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 期数
     */
    private Integer period;
    /**
     * 回款ID
     */
    private Long receivableId;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     *
     */
    private Integer status;
    /**
     * 审批状态
     * 对应字典 {@link cn.iocoder.yudao.module.crm.enums.DictTypeConstants#CRM_RECEIVABLE_CHECK_STATUS}
     */
    private Integer checkStatus;
    /**
     * 工作流编号
     */
    private Long processInstanceId;
    /**
     * 计划回款金额
     */
    private Integer price;
    /**
     * 计划回款日期
     */
    private LocalDateTime returnTime;
    /**
     * 提前几天提醒
     */
    private Integer remindDays;
    /**
     * 提醒日期
     */
    private LocalDateTime remindTime;
    /**
     * 客户ID
     */
    private Long customerId;
    /**
     * 合同ID
     */
    private Long contractId;
    /**
     * 负责人
     */
    private Long ownerUserId;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

}
