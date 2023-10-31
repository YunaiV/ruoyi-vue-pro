package cn.iocoder.yudao.module.product.controller.admin.favorite.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 商品收藏分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductFavoritePageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "5036")
    private Long userId;

    @Schema(description = "商品 SPU 编号", example = "32734")
    private Long spuId;

    @Schema(description = "收藏时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "商品名称", example = "5036")
    private String name;

    @Schema(description = "关键字", example = "5036")
    private String keyword;
}
