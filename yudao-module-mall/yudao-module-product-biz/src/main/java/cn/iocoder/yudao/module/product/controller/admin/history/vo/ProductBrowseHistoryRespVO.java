package cn.iocoder.yudao.module.product.controller.admin.history.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 商品浏览记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductBrowseHistoryRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26055")
    @ExcelProperty("记录编号")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4314")
    @ExcelProperty("用户编号")
    private Long userId;

    @Schema(description = "用户是否删除", example = "false")
    private Boolean userDeleted;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "42")
    @ExcelProperty("商品 SPU 编号")
    private Long spuId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}