package cn.iocoder.yudao.module.hrm.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Schema(description = "管理后台 - HRM 首页日历事项 Response VO")
@Data
public class HrmHomeCalendarItemRespVO {

    @Schema(description = "员工个人备忘编号")
    private Long personalNoteId;

    @Schema(description = "事项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "事项类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "备忘")
    private String typeName;

    @Schema(description = "事项内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "关联业务编号")
    private Long typeId;

    @Schema(description = "事项日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @Schema(description = "事项时间")
    private LocalDateTime eventTime;

}
