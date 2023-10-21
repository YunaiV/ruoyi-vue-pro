package cn.iocoder.yudao.module.crm.dal.dataobject.receivable;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回款管理 DO
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
public class ReceivableDO extends BaseDO {

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
     * 回款计划ID
     *
     * TODO @liuhongfeng：这个字段，后续要写下关联的实体哈
     */
    private Long planId;
    /**
     * 客户ID
     *
     * TODO @liuhongfeng：这个字段，后续要写下关联的实体哈
     */
    private Long customerId;
    /**
     * 合同ID
     *
     * TODO @liuhongfeng：这个字段，后续要写下关联的实体哈
     */
    private Long contractId;
    /**
     * 审批状态
     *
     * 枚举 {@link TODO crm_receivable_check_status 对应的类}
     */
    private Integer checkStatus;
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
    /**
     * 回款方式
     */
    private String returnType;
    /**
     * 回款金额
     */
    private BigDecimal price;
    /**
     * 负责人
     */
    private Long ownerUserId;
    /**
     * 批次
     */
    private Long batchId;
    /**
     * 显示顺序
     */
    private Integer sort;
    // TODO 芋艿：dataScope、dataScopeDeptIds 在想下；
    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    private Integer dataScope;
    /**
     * 数据范围(指定部门数组)
     */
    private String dataScopeDeptIds;
    /**
     * 状态
     *
     * 枚举 {@link TODO common_status 对应的类}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
