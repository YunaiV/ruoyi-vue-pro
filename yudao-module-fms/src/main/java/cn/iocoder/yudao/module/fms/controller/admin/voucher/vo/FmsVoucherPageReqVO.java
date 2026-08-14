package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - FMS 凭证分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsVoucherPageReqVO extends PageParam {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "凭证编号数组")
    @Size(max = 200, message = "凭证编号不能超过 200 个")
    private List<Long> ids;

    @Schema(description = "凭证日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] voucherTime;

    @Schema(description = "凭证字编号", example = "11")
    private Long voucherWordId;

    @Schema(description = "凭证号", example = "1")
    private Integer voucherNumber;

    @Schema(description = "摘要关键词", example = "办公用品")
    private String digest;

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "最小金额", example = "100")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", example = "500")
    private BigDecimal maxAmount;

    @Schema(description = "制单人后台用户编号", example = "1")
    private Long creatorUserId;

    @Schema(description = "审核状态", example = "0")
    @InEnum(FmsVoucherStatusEnum.class)
    private Integer status;

}
