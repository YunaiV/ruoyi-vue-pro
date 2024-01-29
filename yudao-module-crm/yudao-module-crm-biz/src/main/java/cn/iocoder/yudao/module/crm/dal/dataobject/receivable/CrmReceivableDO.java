package cn.iocoder.yudao.module.crm.dal.dataobject.receivable;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

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
    // TODO @liuhongfeng：“对应实体”，参考别的模块，关联 {@link TableField.MetaInfo#getJdbcType()}
    /**
     * 回款计划
     *
     * TODO @liuhongfeng：这个字段什么时候更新，也可以写下
     *
     * 对应实体 {@link CrmReceivablePlanDO}
     */
    private Long planId;
    /**
     * 客户 ID
     *
     * 对应实体 {@link cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO}
     */
    private Long customerId;
    /**
     * 合同 ID
     *
     * 对应实体 {@link CrmContractDO}
     */
    private Long contractId;
    /**
     * 工作流编号
     *
     * TODO @liuhongfeng：这个字段，后续要写下关联的实体哈
     */
    private Long processInstanceId;
    /**
     * 回款日期
     */
    private LocalDateTime returnTime;
    // TODO @liuhongfeng：少个枚举
    /**
     * 回款方式
     */
    private Integer returnType;
    /**
     * 回款金额
     */
    private Integer price;
    // TODO @liuhongfeng：少关联实体；
    /**
     * 负责人
     */
    private Long ownerUserId;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 审核状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum}
     */
    private Integer auditStatus;
    /**
     * 备注
     */
    private String remark;

}
