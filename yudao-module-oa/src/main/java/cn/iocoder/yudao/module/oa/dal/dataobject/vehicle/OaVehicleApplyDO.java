package cn.iocoder.yudao.module.oa.dal.dataobject.vehicle;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleReturnStatusEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 用车申请 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_vehicle_apply", autoResultMap = true)
@KeySequence("oa_vehicle_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaVehicleApplyDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用车申请单号
     */
    private String no;
    /**
     * 车辆编号
     *
     * 关联 {@link OaVehicleDO#getId()}
     */
    private Long vehicleId;
    /**
     * 车牌号快照
     *
     * 冗余 {@link OaVehicleDO#getNo()}
     */
    private String vehicleNo;
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
     * 预计出车时间
     */
    private LocalDateTime startTime;
    /**
     * 预计回车时间
     */
    private LocalDateTime endTime;
    /**
     * 出车地点
     */
    private String startLocation;
    /**
     * 预计回车地点
     */
    private String endLocation;
    /**
     * 用车事由
     */
    private String reason;
    /**
     * 随行人
     */
    private String passenger;
    /**
     * 审批状态
     *
     * 枚举 {@link BpmProcessInstanceStatusEnum}
     */
    private Integer status;
    /**
     * 还车业务状态
     *
     * 枚举 {@link OaVehicleReturnStatusEnum}
     */
    private Integer returnStatus;
    /**
     * BPM 流程实例编号
     *
     * 关联 {@link org.flowable.engine.history.HistoricProcessInstance#getId()}
     */
    private String processInstanceId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;

}
