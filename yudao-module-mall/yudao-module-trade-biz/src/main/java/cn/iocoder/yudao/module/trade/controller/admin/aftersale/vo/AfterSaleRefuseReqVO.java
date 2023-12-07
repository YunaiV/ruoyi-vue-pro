package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 交易售后拒绝收货 Request VO")
@Data
public class AfterSaleRefuseReqVO {

    @Schema(description = "售后编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "售后编号不能为空")
    private Long id;

    @Schema(description = "收货备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "你猜")
    @NotNull(message = "收货备注不能为空")
    private String refuseMemo;

}
