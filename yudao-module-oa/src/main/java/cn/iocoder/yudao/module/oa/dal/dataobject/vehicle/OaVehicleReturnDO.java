package cn.iocoder.yudao.module.oa.dal.dataobject.vehicle;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 还车申请 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_vehicle_return", autoResultMap = true)
@KeySequence("oa_vehicle_return_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaVehicleReturnDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 还车申请单号
     */
    private String no;
    /**
     * 用车申请编号
     *
     * 关联 {@link OaVehicleApplyDO#getId()}
     */
    private Long applyId;
    /**
     * 车辆编号，从用车申请生成
     *
     * 关联 {@link OaVehicleDO#getId()}
     */
    private Long vehicleId;
    /**
     * 申请人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;
    /**
     * 申请部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long deptId;
    /**
     * 实际出车时间
     */
    private LocalDateTime actualStartTime;
    /**
     * 实际出车地点
     */
    private String startLocation;
    /**
     * 用车事由
     */
    private String reason;
    /**
     * 随行人
     */
    private String passenger;
    /**
     * 实际回车时间
     */
    private LocalDateTime actualReturnTime;
    /**
     * 实际回车地点
     */
    private String returnLocation;
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
     * 还车说明
     */
    private String remark;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> fileUrls;

}
