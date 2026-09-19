package cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.meetingroom.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 会议室新增/修改 Request VO")
@Data
public class OaMeetingRoomSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "会议室名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "101 项目讨论室")
    @NotBlank(message = "会议室名称不能为空")
    @Size(max = 100, message = "会议室名称长度不能超过 {max} 个字符")
    private String name;

    @Schema(description = "位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "办公楼一层 101")
    @NotBlank(message = "位置不能为空")
    @Size(max = 255, message = "位置长度不能超过 {max} 个字符")
    private String location;

    @Schema(description = "会议室类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "类型不能为空")
    @InEnum(value = OaMeetingRoomTypeEnum.class, message = "会议室类型不正确")
    private Integer type;

    @Schema(description = "负责人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "负责人不能为空")
    private Long managerUserId;

    @Schema(description = "可用状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "可用状态不能为空")
    @InEnum(value = OaMeetingRoomStatusEnum.class, message = "可用状态不正确")
    private Integer status;

    @Schema(description = "会议室图片", example = "https://www.iocoder.cn/meeting-room.jpg")
    @Size(max = 1024, message = "会议室图片长度不能超过 {max} 个字符")
    private String picUrl;

    @Schema(description = "坐席数", example = "8")
    @Min(value = 1, message = "坐席数不能小于 {value}")
    private Integer seatCount;

    @Schema(description = "设备列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    @NotNull(message = "设备列表不能为空")
    private List<@NotNull(message = "设备列表不能为空") Integer> equipments;

    @Schema(description = "允许预定", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "允许预定不能为空")
    private Boolean allowBooking;

    @Schema(description = "预定需审批", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "预定需审批不能为空")
    private Boolean needApproval;

    @Schema(description = "可预定范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "可预定范围不能为空")
    @InEnum(value = OaMeetingRoomBookingScopeEnum.class, message = "可预定范围不正确")
    private Integer bookingScope;

    @Schema(description = "可预定成员用户编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    @NotNull(message = "可预定成员不能为空")
    private List<@NotNull(message = "可预定成员不能为空") Long> bookingUserIds;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "备注", example = "请提前安排会议室")
    @Size(max = 200, message = "备注长度不能超过 {max} 个字符")
    private String remark;

    @Schema(description = "附件地址列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"https://www.iocoder.cn/meeting.pdf\"]")
    @NotNull(message = "附件地址列表不能为空")
    private List<@NotBlank(message = "附件地址列表不能为空") String> fileUrls;

}
