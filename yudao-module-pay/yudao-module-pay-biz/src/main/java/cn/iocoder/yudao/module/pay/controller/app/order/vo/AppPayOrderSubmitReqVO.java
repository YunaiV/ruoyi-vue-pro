package cn.iocoder.yudao.module.pay.controller.app.order.vo;

import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "用户 APP - 支付订单提交 Request VO")
@Data
public class AppPayOrderSubmitReqVO  extends PayOrderSubmitReqVO {
}
