package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 实验物资 Excel 导出 Request VO，参数和 LabSupplyPageReqVO 是一致的")
@Data
public class LabSupplyExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "物资名称", example = "李四")
    private String name;

    @Schema(description = "物资类型", example = "1")
    private String type;

    @Schema(description = "备注")
    private String mark;

}
