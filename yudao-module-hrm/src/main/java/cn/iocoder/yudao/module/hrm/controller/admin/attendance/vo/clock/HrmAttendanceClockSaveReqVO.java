package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 打卡记录保存 Request VO")
@Data
public class HrmAttendanceClockSaveReqVO {

    @Schema(description = "打卡记录编号", example = "1024")
    private Long id;

    @Schema(description = "打卡员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "打卡员工不能为空")
    private Long employeeId;

    @Schema(description = "打卡时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "打卡时间不能为空")
    private LocalDateTime clockTime;

    @Schema(description = "打卡类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "打卡类型不能为空")
    @InEnum(value = HrmAttendanceClockTypeEnum.class, message = "打卡类型必须是 {value}")
    private Integer type;

    @Schema(description = "应打卡时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应打卡时间不能为空")
    private LocalDateTime attendanceTime;

    @Schema(description = "打卡地址", example = "总部大楼")
    @Size(max = 255, message = "打卡地址不能超过 255 个字符")
    private String address;

    @Schema(description = "经度", example = "121.473701")
    @DecimalMin(value = "-180", message = "经度必须在 -180 到 180 之间")
    @DecimalMax(value = "180", message = "经度必须在 -180 到 180 之间")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "31.230416")
    @DecimalMin(value = "-90", message = "纬度必须在 -90 到 90 之间")
    @DecimalMax(value = "90", message = "纬度必须在 -90 到 90 之间")
    private BigDecimal latitude;

    @Schema(description = "WiFi 名称", example = "office_wifi")
    @Size(max = 50, message = "WiFi 名称不能超过 50 个字符")
    private String ssid;

    @Schema(description = "WiFi MAC 地址", example = "00:11:22:33:44:55")
    @Size(max = 50, message = "WiFi MAC 地址不能超过 50 个字符")
    private String mac;

    @Schema(description = "备注", example = "手工补卡")
    @Size(max = 255, message = "备注不能超过 255 个字符")
    private String remark;

}
