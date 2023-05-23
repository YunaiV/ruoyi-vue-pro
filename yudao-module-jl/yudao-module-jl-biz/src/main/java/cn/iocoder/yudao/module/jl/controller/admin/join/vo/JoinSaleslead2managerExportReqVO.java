package cn.iocoder.yudao.module.jl.controller.admin.join.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 销售线索中的项目售前支持人员 Excel 导出 Request VO，参数和 JoinSaleslead2managerPageReqVO 是一致的")
@Data
public class JoinSaleslead2managerExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "线索id", example = "11347")
    private Long salesleadId;

    @Schema(description = "销售售中人员", example = "23560")
    private Long managerId;

}
