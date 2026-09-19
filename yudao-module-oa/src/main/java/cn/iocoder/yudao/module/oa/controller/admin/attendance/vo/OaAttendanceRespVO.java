package cn.iocoder.yudao.module.oa.controller.admin.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OA 考勤 Response VO")
@Data
@Accessors(chain = true)
public class OaAttendanceRespVO {

    @Schema(description = "考勤记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道")
    private String userName;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "考勤类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "考勤状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "考勤时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    private LocalDateTime attendanceTime;

    @Schema(description = "考勤 IP", example = "127.0.0.1")
    private String attendanceIp;

    @Schema(description = "考勤备注", example = "正常打卡")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
