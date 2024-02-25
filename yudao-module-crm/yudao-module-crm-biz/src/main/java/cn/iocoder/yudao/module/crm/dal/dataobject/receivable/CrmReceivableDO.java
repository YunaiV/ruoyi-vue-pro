package cn.iocoder.yudao.module.crm.dal.dataobject.receivable;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import cn.iocoder.yudao.module.crm.enums.receivable.CrmReceivableReturnTypeEnum;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回款 DO
 *
 * @author 赤焰
 */
@TableName("crm_receivable")
@KeySequence("crm_receivable_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmReceivableDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 回款编号
     */
    private String no;
    /**
     * 回款计划编号
     *
     * 关联 {@link CrmReceivablePlanDO#getId()}，非必须
     */
    private Long planId;
    /**
     * 客户编号
     *
     * 关联 {@link CrmCustomerDO#getId()}
     */
    private Long customerId;
    /**
     * 合同编号
     *
     * 关联 {@link CrmContractDO#getId()}
     */
    private Long contractId;
    /**
     * 负责人编号，关联 {@link AdminUserRespDTO#getId()}
     */
    private Long ownerUserId;

    /**
     * 回款日期
     */
    private LocalDateTime returnTime;
    /**
     * 回款方式,关联枚举{@link CrmReceivableReturnTypeEnum}
     */
    private Integer returnType;
    /**
     * 计划回款金额，单位：元
     */
    private BigDecimal price;
    /**
     * 备注
     */
    private String remark;

    /**
     * 工作流编号
     *
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;
    /**
     * 审批状态
     *
     * 枚举 {@link CrmAuditStatusEnum}
     */
    private Integer auditStatus;

}
