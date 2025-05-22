package cn.iocoder.yudao.module.statistics.controller.admin.product.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 商品统计 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductStatisticsRespVO {

    @Schema(description = "编号，主键自增", requiredMode = Schema.RequiredMode.REQUIRED, example = "12393")
    private Long id;

    @Schema(description = "统计日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-12-16")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @ExcelProperty("统计日期")
    private LocalDate time;

    @Schema(description = "商品SPU编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15114")
    @ExcelProperty("商品SPU编号")
    private Long spuId;

    // region 商品信息

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商品名称")
    @ExcelProperty("商品名称")
    private String name;

    @Schema(description = "商品封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "15114")
    @ExcelProperty("商品封面图")
    private String picUrl;

    // endregion

    @Schema(description = "浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "17505")
    @ExcelProperty("浏览量")
    private Integer browseCount;

    @Schema(description = "访客量", requiredMode = Schema.RequiredMode.REQUIRED, example = "11814")
    @ExcelProperty("访客量")
    private Integer browseUserCount;

    @Schema(description = "收藏数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20950")
    @ExcelProperty("收藏数量")
    private Integer favoriteCount;

    @Schema(description = "加购数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "28493")
    @ExcelProperty("加购数量")
    private Integer cartCount;

    @Schema(description = "下单件数", requiredMode = Schema.RequiredMode.REQUIRED, example = "18966")
    @ExcelProperty("下单件数")
    private Integer orderCount;

    @Schema(description = "支付件数", requiredMode = Schema.RequiredMode.REQUIRED, example = "15142")
    @ExcelProperty("支付件数")
    private Integer orderPayCount;

    @Schema(description = "支付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "11595")
    @ExcelProperty("支付金额，单位：分")
    private Integer orderPayPrice;

    @Schema(description = "退款件数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2591")
    @ExcelProperty("退款件数")
    private Integer afterSaleCount;

    @Schema(description = "退款金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "21709")
    @ExcelProperty("退款金额，单位：分")
    private Integer afterSaleRefundPrice;

    @Schema(description = "访客支付转化率（百分比）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Integer browseConvertPercent;

}