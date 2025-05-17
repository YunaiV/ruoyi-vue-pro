package cn.iocoder.yudao.module.pay.controller.app.order.vo;

import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "用户 APP - 支付订单提交 Request VO")
@Data
public class AppPayOrderSubmitReqVO  extends PayOrderSubmitReqVO {
}
