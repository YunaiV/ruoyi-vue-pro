package cn.iocoder.yudao.module.oms.controller.admin.shop.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OMS 店铺 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OmsShopRespVO {

    @Schema(description = "店铺id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27401")
    @ExcelProperty("店铺id")
    private Long id;

    @Schema(description = "店铺名称")
    @ExcelProperty("店铺名称")
    private String name;

    @Schema(description = "外部来源名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部来源名称")
    private String externalName;

    @Schema(description = "店铺编码")
    @ExcelProperty("店铺编码")
    private String code;

    @Schema(description = "平台店铺编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("平台店铺编码")
    private String platformShopCode;


    @Schema(description = "平台名称")
    @ExcelProperty("平台名称")
    private String platformCode;

    @Schema(description = "店铺类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("店铺类型")
    private Integer type;


    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;
}
