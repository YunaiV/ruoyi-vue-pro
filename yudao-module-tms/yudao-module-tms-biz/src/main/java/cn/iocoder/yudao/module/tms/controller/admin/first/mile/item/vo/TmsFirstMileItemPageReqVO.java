package cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 头程单明细分页 Request VO")
@Data
@ToString(callSuper = true)
public class TmsFirstMileItemPageReqVO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "创建人")
    private Long creator;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

    @Schema(description = "更新人")
    private Long updater;

    @Schema(description = "更新时间")
    private LocalDateTime[] updateTime;

    @Schema(description = "申请项ID")
    private Long requestItemId;

    @Schema(description = "产品ID")
    private Long productId;

    /**
     * FBA条码
     */
    @Schema(description = "FBA条码")
    private String fbaBarCode;

    @Schema(description = "件数")
    private Integer qty;

    @Schema(description = "箱数")
    private Integer[] boxQty;

    @Schema(description = "库存公司")
    private Long companyId;

    @Schema(description = "库存归属部门ID")
    private Long deptId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "实际发货数")
    private Integer[] outboundClosedQty;

    @Schema(description = "计划发货数")
    private Integer[] outboundPlanQty;

    @Schema(description = "已入库数量")
    private Integer[] inboundClosedQty;

    @Schema(description = "发出仓ID")
    private Long fromWarehouseId;

    @Schema(description = "包装长（cm）")
    private BigDecimal[] packageLength;

    @Schema(description = "包装宽（cm）")
    private BigDecimal[] packageWidth;

    @Schema(description = "包装高（cm）")
    private BigDecimal[] packageHeight;

    @Schema(description = "毛重（kg）")
    private BigDecimal[] packageWeight;

    @Schema(description = "体积（m³）")
    private BigDecimal[] volume;

    @Schema(description = "销售公司ID")
    private Long salesCompanyId;

}