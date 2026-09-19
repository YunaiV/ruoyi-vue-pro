package cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 会议室 Response VO")
@Data
public class OaMeetingRoomRespVO {

    @Schema(description = "编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "会议室名称", example = "101 项目讨论室", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "位置", example = "办公楼一层 101", requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @Schema(description = "会议室类型", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "负责人用户编号", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long managerUserId;

    @Schema(description = "可用状态", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "会议室图片", example = "https://www.iocoder.cn/meeting-room.jpg")
    private String picUrl;

    @Schema(description = "坐席数", example = "8")
    private Integer seatCount;

    @Schema(description = "设备列表", example = "[1,2]")
    private List<Integer> equipments;

    @Schema(description = "允许预定", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean allowBooking;

    @Schema(description = "预定需审批", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean needApproval;

    @Schema(description = "可预定范围", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer bookingScope;

    @Schema(description = "可预定成员用户编号列表", example = "[1,2]")
    private List<Long> bookingUserIds;

    @Schema(description = "显示顺序", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "备注", example = "请提前安排会议室")
    private String remark;

    @Schema(description = "附件地址列表", example = "[\"https://www.iocoder.cn/meeting.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "负责人姓名", example = "芋艿")
    private String managerName;

    @Schema(description = "负责人联系方式", example = "13800138000")
    private String managerPhone;

    @Schema(description = "创建时间", example = "1789347600000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    private LocalDateTime createTime;

}
