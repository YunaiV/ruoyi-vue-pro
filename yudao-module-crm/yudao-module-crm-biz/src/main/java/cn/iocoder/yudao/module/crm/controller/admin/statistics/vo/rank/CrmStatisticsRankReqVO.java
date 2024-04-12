package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.rank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 排行榜统计 Request VO")
@Data
public class CrmStatisticsRankReqVO {

    @Schema(description = "部门 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "部门 id 不能为空")
    private Long deptId;

    /**
     * userIds 目前不用前端传递，目前是方便后端通过 deptId 读取编号后，设置回来
     * <p>
     * 后续，可能会支持选择部分用户进行查询
     */
    @Schema(description = "负责人用户 id 集合", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2")
    private List<Long> userIds;

    @Schema(description = "时间范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotEmpty(message = "时间范围不能为空")
    private LocalDateTime[] times;

}
