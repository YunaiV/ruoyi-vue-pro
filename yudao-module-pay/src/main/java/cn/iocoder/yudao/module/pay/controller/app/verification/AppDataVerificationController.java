package cn.iocoder.yudao.module.pay.controller.app.verification;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.controller.app.verification.vo.AppVerifyOrderReqVO;
import cn.iocoder.yudao.module.pay.controller.app.verification.vo.AppVerifyOrderRespVO;
import cn.iocoder.yudao.module.pay.service.blockchain.DataVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 App - 区块链数据验证
 *
 * @author deepay
 */
@Tag(name = "用户 App - 区块链数据验证")
@RestController
@RequestMapping("/pay/verification")
@Validated
public class AppDataVerificationController {

    @Resource
    private DataVerificationService dataVerificationService;

    @PostMapping("/verify")
    @Operation(summary = "验证订单数据真实性（比对链上哈希）")
    @PermitAll
    public CommonResult<AppVerifyOrderRespVO> verifyOrder(@Valid @RequestBody AppVerifyOrderReqVO reqVO) {
        DataVerificationService.VerificationResultDTO dto =
                dataVerificationService.verifyOrder(reqVO.getOrderId(), reqVO.getData());
        AppVerifyOrderRespVO vo = new AppVerifyOrderRespVO();
        vo.setValid(dto.valid);
        vo.setReason(dto.reason);
        vo.setStoredHash(dto.storedHash);
        vo.setProvidedHash(dto.providedHash);
        vo.setTxHash(dto.txHash);
        vo.setChainType(dto.chainType);
        vo.setVerificationTime(dto.verificationTime);
        return success(vo);
    }

    @GetMapping("/link")
    @Operation(summary = "生成可分享的验证链接（1 小时有效）")
    @Parameter(name = "orderId", description = "订单号", required = true)
    public CommonResult<AppVerifyOrderRespVO> generateVerificationLink(@RequestParam("orderId") String orderId) {
        String link = dataVerificationService.generateVerificationLink(orderId);
        AppVerifyOrderRespVO vo = new AppVerifyOrderRespVO();
        vo.setShareLink(link);
        return success(vo);
    }

    @GetMapping("/resolve")
    @Operation(summary = "通过 Token 查询对应订单（校验 Token 合法性）")
    @Parameter(name = "token", description = "验证令牌", required = true)
    @PermitAll
    public CommonResult<String> resolveToken(@RequestParam("token") String token) {
        return success(dataVerificationService.resolveVerificationToken(token));
    }

}
