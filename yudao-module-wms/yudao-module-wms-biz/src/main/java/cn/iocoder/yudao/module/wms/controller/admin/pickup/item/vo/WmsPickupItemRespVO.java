package cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo;

import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : inbound_id,tenant_id,creator,update_time,create_time,bin_id,product_id,qty,id,inbound_item_id,pickup_id,updater
 */
@Schema(description = "管理后台 - 拣货单详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsPickupItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11576")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "拣货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22404")
    @ExcelProperty("拣货单ID")
    private Long pickupId;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20253")
    @ExcelProperty("入库单ID")
    private Long inboundId;

    @Schema(description = "入库单", example = "")
    @ExcelProperty("入库单")
    private WmsInboundSimpleRespVO inbound;

    @Schema(description = "入库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21563")
    @ExcelProperty("入库单明细ID")
    private Long inboundItemId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17566")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "仓位ID，拣货到目标仓位", requiredMode = Schema.RequiredMode.REQUIRED, example = "8732")
    @ExcelProperty("仓位ID")
    private Long binId;

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

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;

    @Schema(description = "拣货数量", example = "")
    @ExcelProperty("拣货数量")
    private Integer qty;

    @Schema(description = "产品", example = "")
    @ExcelProperty("产品")
    private WmsProductRespSimpleVO product;
}
