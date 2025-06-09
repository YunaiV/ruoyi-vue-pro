package cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 调拨单明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TmsTransferItemPageReqVO extends PageParam {

    @Schema(description = "创建人ID")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "更新人ID")
    private String updater;

    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updateTime;

    @Schema(description = "产品id")
    private Long productId;

    @Schema(description = "数量")
    private Integer qty;

    @Schema(description = "箱数")
    private Integer boxQty;

    @Schema(description = "包装重量（kg）")
    private BigDecimal packageWeight;

    @Schema(description = "包装体积（m³）")
    private BigDecimal packageVolume;

    @Schema(description = "库存公司ID")
    private Long stockCompanyId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "实际发货数")
    private Integer outboundClosedQty;

    @Schema(description = "已入库数")
    private Integer inboundClosedQty;

    @Schema(description = "库存归属部门ID")
    private Long deptId;
}