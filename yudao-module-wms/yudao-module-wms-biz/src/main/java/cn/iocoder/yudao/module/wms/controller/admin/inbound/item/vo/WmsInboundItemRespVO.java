package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : inbound_id,tenant_id,source_item_id,left_quantity,creator,update_time,actual_quantity,create_time,product_id,plan_quantity,id,updater
 */
@Schema(description = "管理后台 - 入库单详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30520")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29327")
    @ExcelProperty("入库单ID")
    private Long inboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27659")
    @ExcelProperty("标准产品ID")
    private Long productId;

    @Schema(description = "计划入库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划入库量")
    private Integer planQuantity;

    @Schema(description = "实际入库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("实际入库量")
    private Integer actualQuantity;

    @Schema(description = "批次剩余库存，出库后的剩余库存量")
    @ExcelProperty("批次剩余库存")
    private Integer leftQuantity;

    @Schema(description = "来源详情ID", example = "30830")
    @ExcelProperty("来源详情ID")
    private Long sourceItemId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "产品", example = "")
    @ExcelProperty("产品")
    private ErpProductRespSimpleVO product;
}
