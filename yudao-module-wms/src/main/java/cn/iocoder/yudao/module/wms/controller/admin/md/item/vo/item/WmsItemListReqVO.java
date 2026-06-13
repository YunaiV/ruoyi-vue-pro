package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS 商品列表 Request VO")
@Data
public class WmsItemListReqVO {

    @Schema(description = "商品编号", example = "ITEM001")
    private String code;

    @Schema(description = "商品名称", example = "华为 nova flip")
    private String name;

    @Schema(description = "商品分类编号", example = "1024")
    private Long categoryId;

    @Schema(description = "商品品牌编号", example = "2048")
    private Long brandId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
