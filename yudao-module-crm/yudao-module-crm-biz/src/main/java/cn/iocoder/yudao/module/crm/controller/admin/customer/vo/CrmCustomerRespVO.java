package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 客户 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerRespVO extends CrmCustomerBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long id;

    @Schema(description = "跟进状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean followUpStatus;

    @Schema(description = "锁定状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean lockStatus;

    @Schema(description = "成交状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean dealStatus;

    @Schema(description = "负责人的用户编号", example = "25682")
    private Long ownerUserId;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactLastTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
