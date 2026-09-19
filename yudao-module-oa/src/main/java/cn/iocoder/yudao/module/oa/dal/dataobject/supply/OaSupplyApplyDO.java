package cn.iocoder.yudao.module.oa.dal.dataobject.supply;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import org.flowable.engine.history.HistoricProcessInstance;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 用品领用申请 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_supply_apply", autoResultMap = true)
@KeySequence("oa_supply_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaSupplyApplyDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 申请单号
     */
    private String no;
    /**
     * 申请部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long deptId;
    /**
     * 领用日期
     */
    private LocalDateTime applyTime;
    /**
     * 使用类型
     *
     * 枚举 {@link OaSupplyUseTypeEnum}
     */
    private Integer useType;
    /**
     * 领取方式
     *
     * 字典 {@link DictTypeConstants#SUPPLY_PICKUP_METHOD}
     */
    private Integer pickupMethod;
    /**
     * 申请事由
     */
    private String reason;
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
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;
    /**
     * 备注
     */
    private String remark;

}
