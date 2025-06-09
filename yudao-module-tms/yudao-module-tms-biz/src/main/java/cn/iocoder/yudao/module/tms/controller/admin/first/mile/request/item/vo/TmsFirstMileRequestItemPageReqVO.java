package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 头程申请表明细分页 Request VO")
@Data
@ToString(callSuper = true)
public class TmsFirstMileRequestItemPageReqVO {

    @Schema(description = "明细编号")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

    @Schema(description = "产品编码")
    private String code;

    @Schema(description = "产品id")
    private Long productId;

    @Schema(description = "FBA条码")
    private String fbaBarCode;

    @Schema(description = "申请数量")
    private Integer qty;

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

    @Schema(description = "订购状态")
    private Integer orderStatus;

    @Schema(description = "关闭状态")
    private Integer offStatus;

    @Schema(description = "已订购数")
    private Integer orderClosedQty;

    @Schema(description = "销售公司ID")
    private Long salesCompanyId;

    /**
     * 备注
     */
    private String remark;
}