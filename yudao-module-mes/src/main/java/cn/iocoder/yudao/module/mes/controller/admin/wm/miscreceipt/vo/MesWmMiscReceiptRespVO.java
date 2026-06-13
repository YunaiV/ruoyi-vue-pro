package cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 杂项入库单 Response VO")
@Data
public class MesWmMiscReceiptRespVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "入库单编码", example = "MR2026030001")
    private String code;

    @Schema(description = "入库单名称", example = "退料入库单")
    private String name;

    @Schema(description = "杂项类型", example = "1")
    private Integer type;

    @Schema(description = "来源单据类型", example = "WORK_ORDER")
    private String sourceDocType;
    @Schema(description = "来源单据 ID", example = "1")
    private Long sourceDocId;

    @Schema(description = "来源单据编码", example = "WO2026030001")
    private String sourceDocCode;

    @Schema(description = "入库日期")
    private LocalDateTime receiptDate;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
