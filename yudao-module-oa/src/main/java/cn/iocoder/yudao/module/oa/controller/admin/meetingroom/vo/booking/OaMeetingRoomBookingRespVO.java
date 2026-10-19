package cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 会议室预定 Response VO")
@Data
public class OaMeetingRoomBookingRespVO {

    @Schema(description = "编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "会议室编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomId;

    @Schema(description = "会议主题", example = "产品需求评审", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "会议开始时间", example = "1789351200000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime startTime;

    @Schema(description = "会议结束时间", example = "1789354800000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime endTime;

    @Schema(description = "主持人用户编号", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long moderatorUserId;

    @Schema(description = "参会人用户编号列表", example = "[1,2]")
    private List<Long> attendeeUserIds;

    @Schema(description = "会议提醒", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reminderType;

    @Schema(description = "会议说明", example = "讨论产品需求和排期")
    private String description;

    @Schema(description = "申请备注", example = "请提前安排会议室")
    private String remark;

    @Schema(description = "附件地址列表", example = "[\"https://www.iocoder.cn/meeting.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "申请单号", example = "HY20260914000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String no;

    @Schema(description = "会议室名称", example = "101 项目讨论室")
    private String roomName;

    @Schema(description = "位置", example = "办公楼一层 101")
    private String roomLocation;

    @Schema(description = "会议室类型", example = "1")
    private Integer roomType;

    @Schema(description = "主持人姓名", example = "芋艿")
    private String moderatorName;

    @Schema(description = "参会人姓名", example = "[\"芋艿\",\"小王\"]")
    private List<String> attendeeNames;

    @Schema(description = "申请人", example = "芋艿")
    private String creatorName;

    @Schema(description = "申请部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部门")
    private String deptName;

    @Schema(description = "审批状态", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "使用状态", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer useStatus;

    @Schema(description = "提交时是否需要审批", example = "false")
    private Boolean needApproval;

    @Schema(description = "流程实例编号", example = "process-1024")
    private String processInstanceId;

    @Schema(description = "创建时间", example = "1789347600000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime createTime;

}
