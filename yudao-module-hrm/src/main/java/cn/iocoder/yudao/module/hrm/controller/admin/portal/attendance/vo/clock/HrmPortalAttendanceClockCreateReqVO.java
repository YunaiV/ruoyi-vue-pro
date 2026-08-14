package cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 员工端手机打卡 Request VO")
@Data
public class HrmPortalAttendanceClockCreateReqVO {

    @Schema(description = "打卡地址", example = "上海市浦东新区")
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

}
