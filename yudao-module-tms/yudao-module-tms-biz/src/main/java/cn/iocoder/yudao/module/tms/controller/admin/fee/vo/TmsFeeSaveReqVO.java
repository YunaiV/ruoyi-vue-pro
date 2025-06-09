package cn.iocoder.yudao.module.tms.controller.admin.fee.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 出运订单费用明细新增/修改 Request VO")
@Data
public class TmsFeeSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时，出运订单费用id不能为空")
    @Null(groups = {Validation.OnCreate.class}, message = "创建时，出运订单费用id需为空")
    private Long id;

    @Schema(description = "原单类型;出运订单、调拨单")
    private Integer upstreamType;

    @Schema(description = "原单ID;出运订单ID、调拨单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "原单ID不能为空")
    private Long upstreamId;

    @Schema(description = "费用类型（如运输费、关税）;字典", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "费用类型不能为空")
    @Min(value = 1, message = "费用类型不能小于1")
    private Integer costType;

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @Schema(description = "币种;名称（如 USD、CNY） 字典", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "币种;不能为空")
    @Min(value = 1, message = "币种类型不能小于1")
    private Integer currencyType;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "版本号")
    @NotNull(groups = {Validation.OnUpdate.class}, message = "更新时版本号不能为空")
    private Integer revision;
}