package cn.iocoder.yudao.module.crm.dal.dataobject.receivable;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

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
public class CrmReceivablePlanDO extends BaseDO {

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
     *
     * TODO @liuhongfeng：少关联实体；
     */
    private Long receivableId;
    /**
     * 完成状态
     */
    private Boolean finishStatus;
    /**
     * 计划回款金额，单位：分
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
     * 客户 ID
     *
     * TODO @liuhongfeng：少关联实体；
     */
    private Long customerId;
    /**
     * 合同 ID
     *
     * TODO @liuhongfeng：少关联实体；
     */
    private Long contractId;
    /**
     * 负责人 ID
     *
     * TODO @liuhongfeng：少关联实体；
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
