package cn.iocoder.yudao.module.trade.controller.app.wholesale.pay;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.api.wallet.PayWalletApi;
import cn.iocoder.yudao.module.pay.api.wallet.dto.PayWalletRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.pay.vo.AppWholesalePayRespVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.pay.vo.AppWholesalePaySubmitReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.pay.vo.AppWholesaleRefundReqVO;
import cn.iocoder.yudao.module.trade.service.wholesale.WholesalePaymentService;
import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesalePayResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.pay.bo.WholesaleRefundResultBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 批发订单支付
 * <p>
 * 整合后台现有支付体系（官方 SDK 直接集成）：
 * <ul>
 *   <li>{@code POST /trade/wholesale/pay/create}  — 调用 {@code PayOrderApi} 建立支付单（阿里/微信/余额均走此接口）</li>
 *   <li>{@code GET  /trade/wholesale/pay/get}      — 查询支付单状态</li>
 *   <li>{@code POST /trade/wholesale/pay/refund}   — 调用 {@code PayRefundApi} 发起退款</li>
 *   <li>{@code GET  /trade/wholesale/pay/wallet}   — 调用 {@code PayWalletApi} 查询钱包余额</li>
 * </ul>
 * 渠道提交（选择支付宝/微信/余额提交支付）通过现有 {@code /pay/order/submit} 接口完成，
 * 无需本控制器重复实现。
 *
 * @author deepay
 */
@Tag(name = "用户 App - 批发订单支付（整合 PayOrderApi / PayRefundApi / PayWalletApi）")
@RestController
@RequestMapping("/trade/wholesale/pay")
@Validated
public class AppWholesalePayController {

    @Resource
    private WholesalePaymentService wholesalePaymentService;
    @Resource
    private PayWalletApi payWalletApi;

    // ───────────────────────────────────────────────────────────────────────────
    // 1. 创建支付单
    // ───────────────────────────────────────────────────────────────────────────

    @PostMapping("/create")
    @Operation(summary = "为批发订单创建支付单（调用后台 PayOrderApi，支持支付宝/微信/余额）")
    public CommonResult<AppWholesalePayRespVO> createPayOrder(
            @Valid @RequestBody AppWholesalePaySubmitReqVO reqVO,
            HttpServletRequest request) {
        // 获取用户 IP（网关注入 X-Forwarded-For，也接受手动传入）
        String userIp = resolveUserIp(request, reqVO.getUserIp());

        WholesalePayResultBO result = wholesalePaymentService.createPayOrder(
                getLoginUserId(),
                UserTypeEnum.MEMBER.getValue(),
                reqVO.getMerchantOrderId(),
                reqVO.getSubject(),
                reqVO.getPriceInFen(),
                userIp);

        AppWholesalePayRespVO vo = new AppWholesalePayRespVO();
        vo.setPayResult(result);
        vo.setNextAction(result.getWalletSufficient()
                ? "钱包余额充足（" + result.getWalletBalance() / 100 + " 元）。"
                  + "调用 POST /pay/order/submit 并传 channelCode=WALLET 完成支付。"
                : "调用 POST /pay/order/submit 选择 channelCode（ALIPAY_PC / ALIPAY_APP / "
                  + "WX_JSAPI / WX_APP / WALLET）完成支付。payOrderId=" + result.getPayOrderId());
        return success(vo);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 2. 查询支付单状态
    // ───────────────────────────────────────────────────────────────────────────

    @GetMapping("/get")
    @Operation(summary = "查询批发支付单状态（调用 PayOrderApi.getOrder）")
    @Parameter(name = "payOrderId", description = "支付单编号", required = true)
    public CommonResult<AppWholesalePayRespVO> getPayOrder(@RequestParam("payOrderId") Long payOrderId) {
        WholesalePayResultBO result = wholesalePaymentService.getPayOrder(payOrderId);
        AppWholesalePayRespVO vo = new AppWholesalePayRespVO();
        vo.setPayResult(result);
        return success(vo);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 3. 发起退款
    // ───────────────────────────────────────────────────────────────────────────

    @PostMapping("/refund")
    @Operation(summary = "批发订单发起退款（调用后台 PayRefundApi）")
    public CommonResult<AppWholesalePayRespVO> refund(
            @Valid @RequestBody AppWholesaleRefundReqVO reqVO,
            HttpServletRequest request) {
        String userIp = resolveUserIp(request, reqVO.getUserIp());
        WholesaleRefundResultBO result = wholesalePaymentService.createRefund(
                getLoginUserId(),
                UserTypeEnum.MEMBER.getValue(),
                reqVO.getMerchantOrderId(),
                reqVO.getMerchantRefundId(),
                reqVO.getRefundPrice(),
                reqVO.getReason(),
                userIp);
        AppWholesalePayRespVO vo = new AppWholesalePayRespVO();
        vo.setRefundResult(result);
        return success(vo);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 4. 查询钱包余额
    // ───────────────────────────────────────────────────────────────────────────

    @GetMapping("/wallet")
    @Operation(summary = "查询批发商钱包余额（调用 PayWalletApi）")
    public CommonResult<AppWholesalePayRespVO> getWallet() {
        PayWalletRespDTO wallet = payWalletApi.getOrCreateWallet(
                getLoginUserId(), UserTypeEnum.MEMBER.getValue());
        AppWholesalePayRespVO vo = new AppWholesalePayRespVO();
        vo.setWallet(wallet);
        return success(vo);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 工具
    // ───────────────────────────────────────────────────────────────────────────

    private String resolveUserIp(HttpServletRequest request, String explicitIp) {
        if (explicitIp != null && !explicitIp.isBlank()) {
            return explicitIp;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
