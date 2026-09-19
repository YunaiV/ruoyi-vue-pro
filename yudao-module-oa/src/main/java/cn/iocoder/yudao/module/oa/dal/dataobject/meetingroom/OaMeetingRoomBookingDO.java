package cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.meetingroom.OaMeetingRoomReminderTypeEnum;
import cn.iocoder.yudao.module.oa.enums.meetingroom.OaMeetingRoomUseStatusEnum;
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
 * OA 会议室预定 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_meeting_room_booking", autoResultMap = true)
@KeySequence("oa_meeting_room_booking_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaMeetingRoomBookingDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 预定申请单号
     */
    private String no;
    /**
     * 会议室编号
     *
     * 关联 {@link OaMeetingRoomDO#getId()}
     */
    private Long roomId;
    /**
     * 会议主题
     */
    private String title;
    /**
     * 会议开始时间
     */
    private LocalDateTime startTime;
    /**
     * 会议结束时间
     */
    private LocalDateTime endTime;
    /**
     * 主持人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long moderatorUserId;
    /**
     * 参会用户编号列表
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Long> attendeeUserIds;
    /**
     * 会议提醒类型
     *
     * 枚举 {@link OaMeetingRoomReminderTypeEnum}
     */
    private Integer reminderType;
    /**
     * 是否已发送会议开始提醒
     */
    private Boolean reminded;
    /**
     * 会议说明
     */
    private String description;
    /**
     * 申请部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long deptId;
    /**
     * 审批状态
     *
     * 枚举 {@link BpmProcessInstanceStatusEnum}
     * 无需审批时提交后直接通过
     */
    private Integer status;
    /**
     * BPM 流程实例编号
     *
     * 关联 {@link HistoricProcessInstance#getId()}
     * 无需审批时为空
     */
    private String processInstanceId;
    /**
     * 提交时是否需要审批
     *
     * 取自 {@link OaMeetingRoomDO#getNeedApproval()}，草稿时可为空
     */
    private Boolean needApproval;
    /**
     * 使用状态
     *
     * 枚举 {@link OaMeetingRoomUseStatusEnum}
     */
    private Integer useStatus;
    /**
     * 申请备注
     */
    private String remark;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> fileUrls;

}
