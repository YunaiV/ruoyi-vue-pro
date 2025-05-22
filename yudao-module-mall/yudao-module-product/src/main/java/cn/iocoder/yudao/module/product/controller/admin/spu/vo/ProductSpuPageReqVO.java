package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 商品 SPU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuPageReqVO extends PageParam {

    /**
     * 出售中商品
     */
    public static final Integer FOR_SALE = 0;

    /**
     * 仓库中商品
     */
    public static final Integer IN_WAREHOUSE = 1;

    /**
     * 已售空商品
     */
    public static final Integer SOLD_OUT = 2;

    /**
     * 警戒库存
     */
    public static final Integer ALERT_STOCK = 3;

    /**
     * 商品回收站
     */
    public static final Integer RECYCLE_BIN = 4;

    @Schema(description = "商品名称", example = "清凉小短袖")
    private String name;

    @Schema(description = "前端请求的tab类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer tabType;

    @Schema(description = "商品分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "创建时间", example = "[2022-07-01 00:00:00, 2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
