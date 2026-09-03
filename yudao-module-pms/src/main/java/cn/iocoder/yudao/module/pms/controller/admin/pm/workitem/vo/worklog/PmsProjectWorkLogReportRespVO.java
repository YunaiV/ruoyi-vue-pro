package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - PMS 项目工时报表 Response VO")
@Data
public class PmsProjectWorkLogReportRespVO {

    @Schema(description = "报表日期列表")
    private List<String> dates;

    @Schema(description = "总工时")
    private Integer totalHours;

    @Schema(description = "迭代分组")
    private List<Group> groups;

    @Data
    public static class Group {

        @Schema(description = "迭代编号，未规划事项为空")
        private Long iterationId;

        @Schema(description = "迭代名称")
        private String iterationName;

        @Schema(description = "分组总工时")
        private Integer totalHours;

        @Schema(description = "工作项列表")
        private List<Item> items;

    }

    @Schema(description = "管理后台 - PMS 项目工时报表工作项明细")
    @Data
    public static class Item {

        @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long workItemId;

        @Schema(description = "项目内工作项序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
        private Integer serialNumber;

        @Schema(description = "工作项标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成登录接口")
        private String name;

        @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer type;

        @Schema(description = "工作项总工时", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
        private Integer totalHours;

        @Schema(description = "每日工时，键为日期，值为小时数", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "{\"2026-08-22\": 4}")
        private Map<String, Integer> dailyHours;

    }

}
