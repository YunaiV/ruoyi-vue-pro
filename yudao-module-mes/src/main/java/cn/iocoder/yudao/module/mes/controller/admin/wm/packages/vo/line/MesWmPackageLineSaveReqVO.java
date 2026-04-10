package cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 装箱明细新增/修改 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 装箱明细新增/修改 Request VO")
@Data
public class MesWmPackageLineSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "装箱单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "装箱单 ID 不能为空")
    private Long packageId;

    @Schema(description = "库存记录 ID", example = "1")
    private Long materialStockId;

    @Schema(description = "产品物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品物料不能为空")
    private Long itemId;

    @Schema(description = "装箱数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "装箱数量不能为空")
    @DecimalMin(value = "0.01", message = "装箱数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "生产工单 ID", example = "1")
    private Long workOrderId;

    // DONE @AI：时间都是 LocalDateTIme；
    @Schema(description = "有效期")
    private LocalDateTime expireDate;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
