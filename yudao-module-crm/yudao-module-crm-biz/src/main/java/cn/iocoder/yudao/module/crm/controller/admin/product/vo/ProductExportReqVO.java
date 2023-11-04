package cn.iocoder.yudao.module.crm.controller.admin.product.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 产品 Excel 导出 Request VO，参数和 ProductPageReqVO 是一致的")
@Data
public class ProductExportReqVO {

    @Schema(description = "产品名称", example = "李四")
    private String name;

    @Schema(description = "产品编码")
    private String no;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "价格", example = "8911")
    private Long price;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "产品分类ID", example = "1738")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你说的对")
    private String description;

    @Schema(description = "负责人的用户编号", example = "31926")
    private Long ownerUserId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
