package cn.iocoder.yudao.module.pay.controller.app.order.vo;

import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Schema(description = "用户 APP - 支付订单提交 Response VO")
@Data
public class AppPayOrderSubmitRespVO extends PayOrderSubmitRespVO {

}
