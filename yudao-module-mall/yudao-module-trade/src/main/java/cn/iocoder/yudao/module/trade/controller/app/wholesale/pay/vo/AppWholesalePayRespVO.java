package cn.iocoder.yudao.module.trade.controller.app.wholesale.pay.vo;

import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;
import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesalePayResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesaleRefundResultBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 批发支付 Response VO")
@Data
public class AppWholesalePayRespVO {

    @Schema(description = "支付单创建结果")
    private WholesalePayResultBO payResult;

    @Schema(description = "退款单创建结果")
    private WholesaleRefundResultBO refundResult;

    @Schema(description = "钱包信息（查询余额时返回）")
    private PayWalletRespDTO wallet;

    @Schema(description = "下一步支付操作提示")
    private String nextAction;

    @Schema(description = "支付宝收银台 URL（Alipay SDK 返回）")
    private String alipayPageUrl;

    @Schema(description = "微信支付 prepay_id（WeChat Pay SDK 返回，前端唤起 JSAPI）")
    private String wechatPrepayId;

}
