package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 入库单库存详情扣减 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemFlowRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13478")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23778")
    @ExcelProperty("入库单ID")
    private Long inboundId;

    @Schema(description = "入库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25263")
    @ExcelProperty("入库单明细ID")
    private Long inboundItemId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30952")
    @ExcelProperty("标准产品ID")
    private Long productId;

    @Schema(description = "标准产品SKU", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标准产品SKU")
    private String productSku;

    @Schema(description = "出库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11015")
    @ExcelProperty("出库单ID")
    private Long outboundId;

    @Schema(description = "出库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28163")
    @ExcelProperty("出库单明细ID")
    private Long outboundItemId;

    @Schema(description = "变化的数量，出库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变化的数量，出库量")
    private Integer changedQuantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}