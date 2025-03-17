package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 海关分类子表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpCustomCategoryItemPageReqVO extends PageParam {

//    @Schema(description = "分类表id", example = "25022")
//    private Integer categoryId;

    @Schema(description = "国家-字典")
    private Integer countryCode;

    @Schema(description = "HS编码")
    private String hscode;

    @Schema(description = "税率")
    private BigDecimal taxRate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}