package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 销售线索中竞争对手的报价 Excel 导出 Request VO，参数和 SalesleadCompetitorPageReqVO 是一致的")
@Data
public class SalesleadCompetitorExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "销售线索 id", example = "15754")
    private Long salesleadId;

    @Schema(description = "竞争对手", example = "17682")
    private Long competitorId;

    @Schema(description = "竞争对手的报价")
    private Long competitorQuotation;

}
