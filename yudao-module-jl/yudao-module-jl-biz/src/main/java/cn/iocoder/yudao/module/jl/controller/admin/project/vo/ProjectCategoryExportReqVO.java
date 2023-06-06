package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 项目的实验名目 Excel 导出 Request VO，参数和 ProjectCategoryPageReqVO 是一致的")
@Data
public class ProjectCategoryExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "报价 id", example = "20286")
    private Long quoteId;

    @Schema(description = "安排单 id", example = "14245")
    private Long scheduleId;

    @Schema(description = "类型，报价/安排单", example = "1")
    private String type;

    @Schema(description = "名目的实验类型，动物/细胞/分子等", example = "2")
    private String categoryType;

    @Schema(description = "实验名目库的名目 id", example = "17935")
    private Long categoryId;

    @Schema(description = "实验人员", example = "17520")
    private Long operatorId;

    @Schema(description = "客户需求")
    private String demand;

    @Schema(description = "干扰项")
    private String interference;

    @Schema(description = "依赖项(json数组多个)")
    private String dependIds;

    @Schema(description = "实验名目名字", example = "赵六")
    private String name;

    @Schema(description = "备注")
    private String mark;

}
