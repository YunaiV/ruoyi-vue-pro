package cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 流转卡工序记录 Response VO")
@Data
public class MesProCardProcessRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "流转卡编号", example = "100")
    private Long cardId;

    @Schema(description = "序号", example = "1")
    private Integer sort;

    @Schema(description = "工序编号", example = "200")
    private Long processId;

    @Schema(description = "工序编码", example = "PROC-001")
    private String processCode;

    @Schema(description = "工序名称", example = "焊接")
    private String processName;

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

    @Schema(description = "工位编码", example = "WS-001")
    private String workstationCode;

    @Schema(description = "工位名称", example = "焊接工位 1")
    private String workstationName;

    @Schema(description = "操作人编号", example = "400")
    private Long userId;

    @Schema(description = "操作人名称", example = "张三")
    private String nickname;

    @Schema(description = "过程检验单编号", example = "500")
    private Long ipqcId;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
