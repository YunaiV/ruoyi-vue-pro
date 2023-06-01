package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 实验名目的物资分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CategorySupplyPageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "物资 id", example = "20280")
    private Long supplyId;

    @Schema(description = "名称", example = "物品名字")
    private String name;

    @Schema(description = "规则/单位")
    private String feeStandard;

    @Schema(description = "单价")
    private String unitFee;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "备注")
    private String mark;

    @Schema(description = "实验名目 id", example = "27140")
    private Long categoryId;

}
