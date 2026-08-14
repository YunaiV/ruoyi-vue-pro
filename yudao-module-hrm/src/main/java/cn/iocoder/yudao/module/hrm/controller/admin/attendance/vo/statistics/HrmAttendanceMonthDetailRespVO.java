package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics;

import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工月度考勤详情 Response VO")
@Data
public class HrmAttendanceMonthDetailRespVO {

    @Schema(description = "月度汇总")
    private HrmAttendanceMonthRecordRespVO summary;

    @Schema(description = "每日打卡明细")
    private List<HrmAttendanceDailyDetailRespVO> dailyDetails;

    @Schema(description = "请假记录")
    private List<HrmAttendanceLeaveRespVO> leaves;

}
