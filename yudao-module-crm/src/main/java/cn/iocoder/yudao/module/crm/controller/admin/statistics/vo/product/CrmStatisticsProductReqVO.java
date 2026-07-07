package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 产品分析 Request VO")
@Data
public class CrmStatisticsProductReqVO {

    @Schema(description = "部门 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "部门 id 不能为空")
    private Long deptId;

    @Schema(description = "负责人用户 id", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "负责人用户 id 集合", hidden = true, example = "2")
    private List<Long> userIds;

    @Schema(description = "时间范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotEmpty(message = "请选择时间范围")
    @Size(min = 2, max = 2, message = "请选择时间范围")
    private LocalDateTime[] times;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "10")
    private Long categoryId;

    @Schema(description = "产品分类编号集合", hidden = true, example = "10")
    private List<Long> categoryIds;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "20")
    private Long productId;

}
