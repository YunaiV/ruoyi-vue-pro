package cn.iocoder.yudao.module.oa.controller.admin.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 日程 Response VO")
@Data
public class OaScheduleRespVO {

    @Schema(description = "日程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String creator;

    @Schema(description = "创建人用户昵称", example = "芋道源码")
    private String creatorName;

    @Schema(description = "发布人部门名称", example = "研发部门")
    private String creatorDeptName;

    @Schema(description = "日程类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer priority;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目评审会议")
    private String title;

    @Schema(description = "描述", example = "下周完成盘点差异核实")
    private String description;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    private LocalDateTime endTime;

    @Schema(description = "是否提醒", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean remind;

    @Schema(description = "参与人用户编号列表", example = "[1, 2]")
    private List<Long> participantUserIds;

    @Schema(description = "参与人用户昵称列表", example = "[\"张三\", \"李四\"]")
    private List<String> participantUserNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

    @Schema(description = "参与人阅读信息，仅详情返回")
    private List<Participant> participants;

    @Schema(description = "管理后台 - 日程参与人 Response VO")
    @Data
    public static class Participant {

        @Schema(description = "参与人用户编号", example = "1")
        private Long userId;

        @Schema(description = "参与人用户昵称", example = "芋道源码")
        private String userName;

        @Schema(description = "是否已读", example = "false")
        private Boolean readStatus;

        @Schema(description = "首次阅读时间", type = "integer", format = "int64", example = "1789347600000")
        private LocalDateTime readTime;

    }

}
