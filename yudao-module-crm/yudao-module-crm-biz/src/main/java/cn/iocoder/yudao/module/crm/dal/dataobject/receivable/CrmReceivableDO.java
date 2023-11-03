package cn.iocoder.yudao.module.crm.dal.dataobject.receivable;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

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
     * 对应实体 {@link cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO}
     */
    private Long contractId;
    // TODO @liuhongfeng：“对应字典”，参考别的模块，枚举 {@link XXXX}；另外，这个字段就叫 status，整体状态，不只审批
    /**
     * 审批状态
     * 对应字典 {@link cn.iocoder.yudao.module.crm.enums.DictTypeConstants#CRM_RECEIVABLE_CHECK_STATUS}
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
    // TODO @liuhongfeng：少个枚举
    /**
     * 回款方式
     */
    private String returnType;
    /**
     * 回款金额
     */
    private Integer price;
    // TODO @liuhongfeng：少关联实体；
    /**
     * 负责人
     */
    private Long ownerUserId;
    // TODO @liuhongfeng：应该不需要 batchId 字段
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
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     *
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
