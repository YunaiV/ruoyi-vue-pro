package cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 工艺路线工序 Response VO")
@Data
public class MesProRouteProcessRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeId;

    @Schema(description = "工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long processId;

    @Schema(description = "工序编码")
    private String processCode;

    @Schema(description = "工序名称")
    private String processName;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "下一道工序编号")
    private Long nextProcessId;

    @Schema(description = "下一道工序名称")
    private String nextProcessName;

    @Schema(description = "与下一道工序关系", example = "0")
    private Integer linkType;

    @Schema(description = "准备时间（分钟）", example = "10")
    private Integer prepareTime;

    @Schema(description = "等待时间（分钟）", example = "5")
    private Integer waitTime;

    @Schema(description = "甘特图显示颜色", example = "#00AEF3")
    private String colorCode;

    @Schema(description = "是否关键工序", example = "false")
    private Boolean keyFlag;

    @Schema(description = "是否质检工序", example = "false")
    private Boolean checkFlag;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
