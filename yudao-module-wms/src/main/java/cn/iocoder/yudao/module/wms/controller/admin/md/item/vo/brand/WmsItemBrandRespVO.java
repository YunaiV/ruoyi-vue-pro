package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.brand;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 商品品牌 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsItemBrandRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "品牌编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "B00000001")
    @ExcelProperty("品牌编号")
    private String code;

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "华为")
    @ExcelProperty("品牌名称")
    private String name;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
