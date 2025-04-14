package cn.iocoder.yudao.module.oms.controller.admin.order.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - OMS订单项 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OmsOrderItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29025")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "销售订单id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30317")
    @ExcelProperty("销售订单id")
    private Long orderId;

    @Schema(description = "店铺产品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("店铺产品编码")
    private String shopProductCode;

    @Schema(description = "店铺产品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("店铺产品数量")
    private Integer qty;

    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "1098")
    @ExcelProperty("单价")
    private BigDecimal price;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}