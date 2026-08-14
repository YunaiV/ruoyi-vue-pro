package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics;

import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工每日打卡概况 Response VO")
@Data
public class HrmAttendanceDailyOverviewRespVO {

    @Schema(description = "打卡记录")
    private List<HrmAttendanceClockRespVO> clocks;

    @Schema(description = "考勤结果")
    private String attendanceResult;

    @Schema(description = "打卡概况展示项")
    private List<OverviewItem> overviews;

    @Schema(description = "管理后台 - HRM 员工每日打卡概况展示项 Response VO")
    @Data
    public static class OverviewItem {

        @Schema(description = "打卡类型")
        private String type;

        @Schema(description = "打卡时间")
        private String time;

        @Schema(description = "打卡状态")
        private String status;

        @Schema(description = "考勤结果")
        private String text;

    }

}
