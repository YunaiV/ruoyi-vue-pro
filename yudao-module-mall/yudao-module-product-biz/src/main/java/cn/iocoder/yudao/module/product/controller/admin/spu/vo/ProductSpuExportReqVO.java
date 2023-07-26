package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 商品 SPU 导出 Request VO，参数和 ProductSpuPageReqVO 是一致的")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpuExportReqVO {

    @Schema(description = "商品名称", example = "清凉小短袖")
    private String name;

    @Schema(description = "前端请求的tab类型", example = "1")
    private Integer tabType;

    @Schema(description = "商品分类编号", example = "100")
    private Long categoryId;

    @Schema(description = "创建时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
