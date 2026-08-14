package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 打卡记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HrmAttendanceClockRespVO {

    @Schema(description = "打卡记录编号", example = "1024")
    @ExcelProperty(value = "记录编号", index = 0)
    private Long id;

    @Schema(description = "打卡员工编号", example = "1001")
    @ExcelProperty(value = "员工编号", index = 1)
    private Long employeeId;

    @Schema(description = "员工姓名", example = "芋道")
    @ExcelProperty(value = "员工姓名", index = 2)
    private String employeeName;

    @Schema(description = "工号", example = "HRM001")
    @ExcelProperty(value = "工号", index = 3)
    private String jobNumber;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    @ExcelProperty(value = "部门", index = 4)
    private String deptName;

    @Schema(description = "职位名称", example = "Java 工程师")
    @ExcelProperty(value = "岗位", index = 5)
    private String postName;

    @Schema(description = "打卡时间")
    @ExcelProperty(value = "打卡时间", index = 8)
    private LocalDateTime clockTime;

    @Schema(description = "打卡类型", example = "1")
    @ExcelProperty(value = "打卡类型", index = 6, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_ATTENDANCE_CLOCK_TYPE)
    private Integer type;

    @Schema(description = "应打卡时间")
    @ExcelProperty(value = "应打卡时间", index = 7)
    private LocalDateTime attendanceTime;

    @Schema(description = "打卡来源")
    @ExcelProperty(value = "来源", index = 10, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_ATTENDANCE_CLOCK_SOURCE)
    private Integer sourceType;

    @Schema(description = "打卡状态")
    @ExcelProperty(value = "打卡状态", index = 9, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_ATTENDANCE_CLOCK_STATUS)
    private Integer status;

    @Schema(description = "打卡阶段")
    private Integer stage;

    @Schema(description = "打卡地址", example = "总部大楼")
    @ExcelProperty(value = "地点", index = 11)
    private String address;

    @Schema(description = "经度", example = "121.473701")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "31.230416")
    private BigDecimal latitude;

    @Schema(description = "WiFi 名称", example = "office_wifi")
    private String ssid;

    @Schema(description = "WiFi MAC 地址", example = "00:11:22:33:44:55")
    private String mac;

    @Schema(description = "备注", example = "手工补卡")
    @ExcelProperty(value = "备注", index = 12)
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
