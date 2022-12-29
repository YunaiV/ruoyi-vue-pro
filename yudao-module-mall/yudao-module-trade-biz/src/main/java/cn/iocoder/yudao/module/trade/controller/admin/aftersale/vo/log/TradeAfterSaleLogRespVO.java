package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.log;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel("管理后台 - 交易售后日志 Response VO")
@Data
public class TradeAfterSaleLogRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "20669")
    private Long id;

    @ApiModelProperty(value = "用户编号", required = true, example = "22634")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @ApiModelProperty(value = "用户类型", required = true, example = "2")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @ApiModelProperty(value = "售后编号", required = true, example = "3023")
    @NotNull(message = "售后编号不能为空")
    private Long afterSaleId;

    @ApiModelProperty(value = "订单编号", required = true, example = "25870")
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @ApiModelProperty(value = "订单项编号", required = true, example = "23154")
    @NotNull(message = "订单项编号不能为空")
    private Long orderItemId;

    @ApiModelProperty(value = "售后状态（之前）", example = "2", notes = "参见 TradeAfterSaleStatusEnum 枚举")
    private Integer beforeStatus;

    @ApiModelProperty(value = "售后状态（之后）", required = true, example = "1", notes = "参见 TradeAfterSaleStatusEnum 枚举")
    @NotNull(message = "售后状态（之后）不能为空")
    private Integer afterStatus;

    @ApiModelProperty(value = "操作明细", required = true, example = "维权完成，退款金额：¥37776.00")
    @NotNull(message = "操作明细不能为空")
    private String content;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
