package cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工端考勤记录 Response VO")
@Data
public class HrmPortalAttendanceRecordRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "打卡时间")
    private LocalDateTime clockTime;

    @Schema(description = "员工端考勤记录类型")
    private Integer type;

    @Schema(description = "应打卡时间")
    private LocalDateTime attendanceTime;

    @Schema(description = "来源类型")
    private Integer sourceType;

    @Schema(description = "员工端考勤记录状态")
    private Integer status;

    @Schema(description = "阶段")
    private Integer stage;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "WiFi 名称")
    private String ssid;

    @Schema(description = "WiFi MAC 地址")
    private String mac;

    @Schema(description = "备注")
    private String remark;

}
