package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 供应商产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SrmSupplierProductPageReqVO extends PageParam {

    @Schema(description = "供应商产品编码")
    private String code;

    @Schema(description = "供应商编号", example = "29689")
    private Long supplierId;

    @Schema(description = "产品编号", example = "26097")
    private Long productId;

    @Schema(description = "包装高度")
    private Double packageHeight;

    @Schema(description = "包装长度")
    private Double packageLength;

    @Schema(description = "包装重量")
    private Double packageWeight;

    @Schema(description = "包装宽度")
    private Double packageWidth;

    @Schema(description = "采购价格", example = "25304")
    private Double purchasePrice;

    @Schema(description = "采购货币代码")
    private Integer purchasePriceCurrencyCode;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}