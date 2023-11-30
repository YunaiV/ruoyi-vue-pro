package cn.iocoder.yudao.module.crm.dal.dataobject.contract;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

// TODO 芋艿：实体的梳理
/**
 * CRM 合同 DO
 *
 * @author dhb52
 */
@TableName("crm_contract")
@KeySequence("crm_contract_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmContractDO extends BaseDO {

    /**
     * 合同编号
     */
    @TableId
    private Long id;
    /**
     * 合同名称
     */
    private String name;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 商机编号
     */
    private Long businessId;
    /**
     * 工作流编号
     */
    private Long processInstanceId;
    /**
     * 下单日期
     */
    private LocalDateTime orderDate;
    /**
     * 合同编号
     */
    private String no;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 合同金额
     */
    private Integer price;
    /**
     * 整单折扣
     */
    private Integer discountPercent;
    /**
     * 产品总金额
     */
    private Integer productPrice;
    /**
     * 联系人编号
     */
    private Long contactId;
    /**
     * 公司签约人
     */
    private Long signUserId;
    /**
     * 最后跟进时间
     */
    private LocalDateTime contactLastTime;
    /**
     * 备注
     */
    private String remark;

    /**
     * 负责人的用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;

    /**
     * 审批状态
     *
     * 枚举 {@link CrmAuditStatusEnum}
     */
    private Integer auditStatus;

}
