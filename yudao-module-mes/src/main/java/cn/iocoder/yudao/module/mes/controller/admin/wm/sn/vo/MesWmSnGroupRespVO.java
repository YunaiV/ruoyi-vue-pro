package cn.iocoder.yudao.module.mes.controller.admin.wm.sn.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES SN 码分组 Response VO")
@Data
@Accessors(chain = true)
public class MesWmSnGroupRespVO {

    @Schema(description = "批次 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String uuid;

    @Schema(description = "SN 码数量", example = "100")
    @ExcelProperty("SN 码数量")
    private Integer count;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "ITEM001")
    @ExcelProperty("物料编码")
    private String itemCode;

    @Schema(description = "物料名称", example = "物料A")
    @ExcelProperty("物料名称")
    private String itemName;

    @Schema(description = "规格型号", example = "100*200")
    @ExcelProperty("规格型号")
    private String specification;

    @Schema(description = "批次号", example = "BATCH001")
    @ExcelProperty("批次号")
    private String batchCode;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "创建时间")
    @ExcelProperty("生成时间")
    private LocalDateTime createTime;

}
