package cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 流转卡工序记录新增/修改 Request VO")
@Data
public class MesProCardProcessSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "流转卡编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "流转卡不能为空")
    private Long cardId;

    @Schema(description = "序号", example = "1")
    private Integer sort;

    @Schema(description = "工序编号", example = "200")
    private Long processId;

    @Schema(description = "进入工序时间")
    private LocalDateTime inputTime;

    @Schema(description = "出工序时间")
    private LocalDateTime outputTime;

    @Schema(description = "投入数量", example = "100.00")
    private BigDecimal inputQuantity;

    @Schema(description = "产出数量", example = "98.00")
    private BigDecimal outputQuantity;

    @Schema(description = "不合格品数量", example = "2.00")
    private BigDecimal unqualifiedQuantity;

    @Schema(description = "工位编号", example = "300")
    private Long workstationId;

    @Schema(description = "操作人编号", example = "400")
    private Long userId;

    @Schema(description = "过程检验单编号", example = "500")
    private Long ipqcId;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
