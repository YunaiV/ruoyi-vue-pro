package cn.iocoder.yudao.module.crm.dal.dataobject.receivable;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.receivable.CrmReceivableReturnTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CRM 回款计划 DO
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
public class CrmReceivablePlanDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 期数
     */
    private Integer period;
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
     * 负责人编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;

    /**
     * 计划回款日期
     */
    private LocalDateTime returnTime;
    /**
     * 计划回款类型
     *
     * 枚举 {@link CrmReceivableReturnTypeEnum}
     */
    private Integer returnType;
    /**
     * 计划回款金额，单位：元
     */
    private BigDecimal price;

    /**
     * 回款编号，关联 {@link CrmReceivableDO#getId()}
     */
    private Long receivableId;

    /**
     * 提前几天提醒
     */
    private Integer remindDays;
    /**
     * 提醒日期
     */
    private LocalDateTime remindTime;
    /**
     * 备注
     */
    private String remark;

}
