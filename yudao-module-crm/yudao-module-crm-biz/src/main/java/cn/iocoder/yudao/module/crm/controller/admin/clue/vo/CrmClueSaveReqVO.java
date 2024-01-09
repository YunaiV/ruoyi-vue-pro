package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.framework.common.validation.Telephone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author minhx
 */
@Schema(description = "管理后台 - CRM 线索 创建/更新 Request VO")
@Data
public class CrmClueSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10969")
    private Long id;

    @Schema(description = "线索名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "线索xxx")
    @NotEmpty(message = "线索名称不能为空")
    private String name;

    @Schema(description = "客户 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "520")
    private Long customerId;

    @Schema(description = "下次联系时间", example = "2023-10-18 01:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

    @Schema(description = "电话", example = "18000000000")
    @Telephone
    private String telephone;

    @Schema(description = "手机号", example = "18000000000")
    @Mobile
    private String mobile;

    @Schema(description = "地址", example = "北京市海淀区")
    private String address;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactLastTime;

    @Schema(description = "负责人编号")
    private Long ownerUserId;

    @Schema(description = "备注", example = "随便")
    private String remark;
}
