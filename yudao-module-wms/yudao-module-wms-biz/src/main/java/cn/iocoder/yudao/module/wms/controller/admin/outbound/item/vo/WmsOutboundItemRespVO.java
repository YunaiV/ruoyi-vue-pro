package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,create_time,bin_id,plan_quantity,outbound_id,updater,source_item_id,update_time,outbound_status,actual_quantity,product_id,id
 */
@Schema(description = "管理后台 - 出库单详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOutboundItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "27153")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6602")
    @ExcelProperty("入库单ID")
    private Long outboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20572")
    @ExcelProperty("标准产品ID")
    private Long productId;

    @Schema(description = "实际出库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("实际出库量")
    private Integer actualQuantity;

    @Schema(description = "来源详情ID", example = "11448")
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

    @Schema(description = "产品", example = "")
    @ExcelProperty("产品")
    private ErpProductRespSimpleVO product;

    @Schema(description = "出库单", example = "")
    @ExcelProperty("出库单")
    private WmsInboundRespVO outbound;

    @Schema(description = "出库状态 ; OutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库", example = "")
    @ExcelProperty("出库状态")
    private Integer outboundStatus;

    @Schema(description = "计划出库量", example = "")
    @ExcelProperty("计划出库量")
    private Integer planQuantity;

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

    @Schema(description = "出库库位ID", example = "")
    @ExcelProperty("出库库位ID")
    private Long binId;
}
