package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 项目报价分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectQuotePageReqVO extends PageParam {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "销售线索 id", example = "23498")
    private Long salesleadId;

    @Schema(description = "项目 id", example = "21263")
    private Long projectId;

    @Schema(description = "报价单的名字", example = "张三")
    private String name;

    @Schema(description = "方案 URL", example = "https://www.iocoder.cn")
    private String reportUrl;

    @Schema(description = "折扣(100: 无折扣, 98: 98折)", example = "6647")
    private Integer discount;

    @Schema(description = "状态, 已提交、已作废、已采用", example = "1")
    private String status;

}
