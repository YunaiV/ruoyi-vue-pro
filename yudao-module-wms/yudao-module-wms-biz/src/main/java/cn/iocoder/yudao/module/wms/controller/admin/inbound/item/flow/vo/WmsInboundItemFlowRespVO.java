package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,outbound_qty,create_time,outbound_id,outbound_item_id,updater,inbound_id,outbound_action_id,update_time,product_id,id,inbound_item_id
 */
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

    @Schema(description = "出库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11015")
    @ExcelProperty("出库单ID")
    private Long outboundId;

    @Schema(description = "出库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28163")
    @ExcelProperty("出库单明细ID")
    private Long outboundItemId;

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

    @Schema(description = "变化的数量，出库量", example = "")
    @ExcelProperty("变化的数量")
    private Integer outboundQty;

    @Schema(description = "出库动作ID", example = "")
    @ExcelProperty("出库动作ID")
    private Long outboundActionId;
}
