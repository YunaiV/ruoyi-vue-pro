package cn.iocoder.yudao.module.oa.dal.dataobject.travel;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.flowable.engine.history.HistoricProcessInstance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 出差报销 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_travel_reimbursement", autoResultMap = true)
@KeySequence("oa_travel_reimbursement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaTravelReimbursementDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 单据编号
     */
    private String no;
    /**
     * 申请部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     * 保存申请时的部门，不随申请人后续调岗变化
     */
    private Long deptId;
    /**
     * 关联出差申请编号
     *
     * 关联 {@link OaTravelApplyDO#getId()}，未关联申请时为空
     */
    private Long travelApplyId;
    /**
     * 出差事由
     */
    private String reason;
    /**
     * 出差开始时间
     */
    private LocalDateTime startTime;
    /**
     * 出差结束时间
     */
    private LocalDateTime endTime;
    /**
     * 出差天数
     *
     * 冗余字段，由开始和结束时间的时长除以 24 小时计算，向上取整
     */
    private Integer days;
    /**
     * 报销总金额，单位：元
     *
     * 冗余 {@link #getItems()} 中 {@link Item#getPrice()} 的合计
     */
    private BigDecimal totalPrice;
    /**
     * 是否已支付
     */
    private Boolean payStatus;
    /**
     * 审批状态
     *
     * 枚举 {@link BpmProcessInstanceStatusEnum}
     * 冗余 {@link #getProcessInstanceId()} 对应的流程实例状态，与出差申请的审批状态独立
     */
    private Integer status;
    /**
     * BPM 流程实例编号
     *
     * 关联 {@link HistoricProcessInstance#getId()}
     */
    private String processInstanceId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 费用明细
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Item> items;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;

    /**
     * 费用明细
     */
    @Data
    public static class Item {

        /**
         * 费用类型
         *
         * 字典 {@link DictTypeConstants#EXPENSE_TYPE}
         */
        private Integer expenseType;
        /**
         * 费用发生时间
         */
        private LocalDateTime expenseTime;
        /**
         * 出发地
         */
        private String departureCity;
        /**
         * 到达地
         */
        private String arrivalCity;
        /**
         * 金额，单位：元
         */
        private BigDecimal price;
        /**
         * 费用说明
         */
        private String description;

    }

}
