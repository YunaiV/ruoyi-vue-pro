package cn.iocoder.yudao.module.oa.controller.admin.overtime.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 加班申请 Response VO")
@Data
public class OaOvertimeApplyRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "标题", example = "项目上线加班申请")
    private String title;

    @Schema(description = "紧急程度", example = "1")
    private Integer urgency;

    @Schema(description = "加班类型", example = "1")
    private Integer type;

    @Schema(description = "开始时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", type = "integer", format = "int64", example = "1789380000000")
    private LocalDateTime endTime;

    @Schema(description = "天数", example = "1")
    private BigDecimal days;

    @Schema(description = "申请原因", example = "完成项目上线前的联调工作")
    private String reason;

    @Schema(description = "审批状态", example = "-1")
    private Integer status;

    @Schema(description = "流程实例编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "申请人编号", example = "1024")
    private String creator;

    @Schema(description = "申请人昵称", example = "张三")
    private String creatorName;

    @Schema(description = "申请时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;
}
