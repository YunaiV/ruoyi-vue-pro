package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 实验收费项 Excel 导出 Request VO，参数和 ChargeItemPageReqVO 是一致的")
@Data
public class ChargeItemExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "收费类型", example = "1")
    private String type;

    @Schema(description = "名称", example = "李四")
    private String name;

    @Schema(description = "成本价", example = "10320")
    private Long costPrice;

    @Schema(description = "建议销售价", example = "5530")
    private Long suggestedSellingPrice;

    @Schema(description = "备注")
    private String mark;

}
