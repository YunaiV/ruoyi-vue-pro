package cn.iocoder.yudao.module.mes.controller.admin.cal.calendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - MES 排班日历 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCalCalendarRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-01-15 00:00:00")
    private LocalDateTime day;

    @Schema(description = "轮班方式", example = "2")
    private Integer shiftType; // 对应 MesCalShiftTypeEnum 枚举值

    @Schema(description = "班组排班列表")
    private List<TeamShiftItem> teamShifts;

    @Schema(description = "班组排班项")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamShiftItem {

        @Schema(description = "班组编号", example = "201")
        private Long teamId;

        @Schema(description = "班组名称", example = "注塑A组")
        private String teamName;

        @Schema(description = "班次编号", example = "1")
        private Long shiftId;

        @Schema(description = "班次名称", example = "白班")
        private String shiftName;

        @Schema(description = "排序", example = "1")
        private Integer sort;

    }

}
