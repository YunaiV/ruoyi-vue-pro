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
 * OA 出差申请 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_travel_apply", autoResultMap = true)
@KeySequence("oa_travel_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaTravelApplyDO extends BaseDO {

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
     */
    private Long deptId;
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
     */
    private Integer days;
    /**
     * 同行人
     *
     * 填写姓名文本，不关联用户编号
     */
    private String companion;
    /**
     * 预计费用，单位：元
     */
    private BigDecimal estimatedPrice;
    /**
     * 是否已报销
     */
    private Boolean reimburseStatus;
    /**
     * 审批状态
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
     * 备注
     */
    private String remark;
    /**
     * 行程明细
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Item> items;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;

    /**
     * 行程明细
     */
    @Data
    public static class Item {

        /**
         * 出发地区编号
         *
         * 关联 {@link cn.iocoder.yudao.framework.ip.core.Area#getId()}
         */
        private Integer departureAreaId;
        /**
         * 到达地区编号
         *
         * 关联 {@link cn.iocoder.yudao.framework.ip.core.Area#getId()}
         */
        private Integer arrivalAreaId;
        /**
         * 行程开始时间
         */
        private LocalDateTime startTime;
        /**
         * 行程结束时间
         */
        private LocalDateTime endTime;
        /**
         * 交通方式
         *
         * 字典 {@link DictTypeConstants#TRANSPORT_TYPE}
         */
        private Integer transportType;
        /**
         * 备注
         */
        private String remark;

    }

}
