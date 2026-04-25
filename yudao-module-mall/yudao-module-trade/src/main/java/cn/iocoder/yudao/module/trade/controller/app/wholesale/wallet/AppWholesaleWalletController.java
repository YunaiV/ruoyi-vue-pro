package cn.iocoder.yudao.module.trade.controller.app.wholesale.wallet;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.wallet.vo.AppWholesaleWalletPayReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.wallet.vo.AppWholesaleWalletRechargeReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.wallet.vo.AppWholesaleWalletRespVO;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.WholesaleWalletDeepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 批发商钱包深度集成
 * <p>
 * 直接对接后台钱包（PayWalletApi）+ 支付单（PayOrderApi）：
 * <ul>
 *   <li>{@code GET  /trade/wholesale/wallet/summary}  — 查询钱包余额摘要</li>
 *   <li>{@code POST /trade/wholesale/wallet/pay}       — 钱包余额直接扣款（无需跳转第三方）</li>
 *   <li>{@code POST /trade/wholesale/wallet/recharge}  — 发起充值支付单（支付宝/微信完成充值）</li>
 * </ul>
 *
 * @author deepay
 */
@Tag(name = "用户 App - 批发商钱包（深度集成 PayWalletApi + PayOrderApi）")
@RestController
@RequestMapping("/trade/wholesale/wallet")
@Validated
public class AppWholesaleWalletController {

    @Resource
    private WholesaleWalletDeepService walletDeepService;

    // ──────────────────────────────────────────────────────────────────────────
    // 1. 查询钱包余额摘要
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/summary")
    @Operation(summary = "查询批发商钱包余额摘要（调用 PayWalletApi.getOrCreateWallet）")
    public CommonResult<AppWholesaleWalletRespVO> getWalletSummary() {
        AppWholesaleWalletRespVO vo = new AppWholesaleWalletRespVO();
        vo.setSummary(walletDeepService.getWalletSummary(
                getLoginUserId(), UserTypeEnum.MEMBER.getValue()));
        return success(vo);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. 钱包直接扣款（余额优先支付）
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/pay")
    @Operation(summary = "批发订单钱包直接扣款（调用 PayWalletApi.addWalletBalance(-price)）")
    public CommonResult<AppWholesaleWalletRespVO> walletPay(
            @Valid @RequestBody AppWholesaleWalletPayReqVO reqVO) {
        AppWholesaleWalletRespVO vo = new AppWholesaleWalletRespVO();
        vo.setDeductResult(walletDeepService.deductWalletForOrder(
                getLoginUserId(),
                UserTypeEnum.MEMBER.getValue(),
                reqVO.getMerchantOrderId(),
                reqVO.getPriceInFen()));
        return success(vo);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. 发起充值支付单
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/recharge")
    @Operation(summary = "发起钱包充值支付单（支付宝/微信完成充值，调用 PayOrderApi.createOrder）")
    public CommonResult<AppWholesaleWalletRespVO> createRecharge(
            @Valid @RequestBody AppWholesaleWalletRechargeReqVO reqVO,
            HttpServletRequest request) {
        String userIp = resolveUserIp(request, reqVO.getUserIp());
        AppWholesaleWalletRespVO vo = new AppWholesaleWalletRespVO();
        vo.setRechargeResult(walletDeepService.createRechargePayOrder(
                getLoginUserId(),
                UserTypeEnum.MEMBER.getValue(),
                reqVO.getRechargeYuan(),
                userIp));
        return success(vo);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 工具
    // ──────────────────────────────────────────────────────────────────────────

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
