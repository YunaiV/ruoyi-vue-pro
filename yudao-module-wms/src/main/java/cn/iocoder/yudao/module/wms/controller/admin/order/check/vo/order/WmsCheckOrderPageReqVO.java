package cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS 盘库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsCheckOrderPageReqVO extends PageParam {

    @Schema(description = "盘库单号", example = "PK202605110001")
    private String no;

    @Schema(description = "单据状态", example = "0")
    @InEnum(WmsOrderStatusEnum.class)
    private Integer status;

    @Schema(description = "仓库编号", example = "1024")
    private Long warehouseId;

    @Schema(description = "单据日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderTime;

    @Schema(description = "最小盈亏数量", example = "-10.00")
    private BigDecimal totalQuantityMin;

    @Schema(description = "最大盈亏数量", example = "100.00")
    private BigDecimal totalQuantityMax;

    @Schema(description = "最小总金额", example = "1.00")
    private BigDecimal totalPriceMin;

    @Schema(description = "最大总金额", example = "1000.00")
    private BigDecimal totalPriceMax;

    @Schema(description = "最小实际金额", example = "1.00")
    private BigDecimal actualPriceMin;

    @Schema(description = "最大实际金额", example = "1000.00")
    private BigDecimal actualPriceMax;

    @Schema(description = "创建用户", example = "1")
    private String creator;

    @Schema(description = "更新用户", example = "1")
    private String updater;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] updateTime;

}
