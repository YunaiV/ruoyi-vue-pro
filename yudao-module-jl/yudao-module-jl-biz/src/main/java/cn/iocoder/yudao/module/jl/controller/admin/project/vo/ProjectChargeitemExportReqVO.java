package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 项目中的实验名目的收费项 Excel 导出 Request VO，参数和 ProjectChargeitemPageReqVO 是一致的")
@Data
public class ProjectChargeitemExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "选中的实验名目 id", example = "11056")
    private Long projectCategoryId;

    @Schema(description = "原始的实验名目 id", example = "10361")
    private Long categoryId;

    @Schema(description = "物资 id", example = "5750")
    private Long chargeItemId;

    @Schema(description = "名称", example = "李四")
    private String name;

    @Schema(description = "规则/单位")
    private String feeStandard;

    @Schema(description = "单价")
    private String unitFee;

    @Schema(description = "单量")
    private Integer unitAmount;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "备注")
    private String mark;

}
