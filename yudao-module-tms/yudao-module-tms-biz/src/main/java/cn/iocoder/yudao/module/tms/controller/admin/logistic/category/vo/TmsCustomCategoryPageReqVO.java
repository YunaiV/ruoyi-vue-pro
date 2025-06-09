package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 海关分类分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TmsCustomCategoryPageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "材质-字典")
    private Integer material;

    @Schema(description = "报关品名", example = "2")
    private String declaredType;

    @Schema(description = "英文品名")
    private String declaredTypeEn;

    @Schema(description = "用途描述")
    private String customsPurpose;

    @Schema(description = "报关材质描述")
    private String customsMaterial;
}