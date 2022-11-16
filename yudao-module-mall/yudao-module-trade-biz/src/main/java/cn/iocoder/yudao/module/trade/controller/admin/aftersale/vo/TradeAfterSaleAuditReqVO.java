package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 交易售后审批 Request VO")
@Data
public class TradeAfterSaleAuditReqVO {

    @ApiModelProperty(value = "售后编号", required = true, example = "1024")
    @NotNull(message = "售后编号不能为空")
    private Long id;

    @ApiModelProperty(value = "审批结果", required = true, example = "true",
            notes = "true - 通过；false - 不通过")
    @NotNull(message = "审批结果不能为空")
    private Boolean audit;

    @ApiModelProperty(value = "审批备注", example = "你猜")
    private String auditReason;

}
