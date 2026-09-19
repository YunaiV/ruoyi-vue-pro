package cn.iocoder.yudao.module.oa.dal.dataobject.reimbursement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.flowable.engine.history.HistoricProcessInstance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 费用报销 DO
 *
 * 申请人使用 {@link BaseDO#getCreator()}，申请时间使用 {@link BaseDO#getCreateTime()}
 *
 * @author 芋道源码
 */
@TableName(value = "oa_reimbursement", autoResultMap = true)
@KeySequence("oa_reimbursement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaReimbursementDO extends BaseDO {

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
     * 证明人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long witnessUserId;
    /**
     * 相关客户名称
     *
     * 填写客户名称文本，不关联客户档案
     */
    private String customerName;
    /**
     * 报销方式
     *
     * 字典 {@link DictTypeConstants#REIMBURSEMENT_PAYMENT_METHOD}
     */
    private Integer paymentMethod;
    /**
     * 票据总数
     *
     * 汇总 {@link Item#getInvoiceCount()}，不由用户单独填写
     */
    private Integer invoiceCount;
    /**
     * 报销总金额，单位：元
     *
     * 汇总 {@link Item#getPrice()}，不由用户单独填写
     */
    private BigDecimal totalPrice;
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
    /**
     * 报销明细
     *
     * 明细随报销单保存，不维护独立编号及生命周期
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Item> items;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> fileUrls;

    /**
     * 报销明细
     */
    @Data
    public static class Item {

        /**
         * 费用产生时间
         */
        private LocalDateTime expenseTime;
        /**
         * 费用类型
         *
         * 字典 {@link DictTypeConstants#EXPENSE_TYPE}
         */
        private Integer expenseType;
        /**
         * 费用说明
         */
        private String description;
        /**
         * 票据张数
         */
        private Integer invoiceCount;
        /**
         * 报销金额，单位：元
         */
        private BigDecimal price;

    }

}
