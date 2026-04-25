package cn.iocoder.yudao.module.product.controller.app.share;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.app.share.vo.AppProductShareCreateReqVO;
import cn.iocoder.yudao.module.product.controller.app.share.vo.AppProductShareRespVO;
import cn.iocoder.yudao.module.product.service.share.ProductShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 安全分享（IntelligentSharingSystem + SecureSharingService）
 *
 * @author deepay
 */
@Tag(name = "用户 App - 安全分享")
@RestController
@RequestMapping("/product/share")
@Validated
public class AppProductShareController {

    @Resource
    private ProductShareService productShareService;

    @PostMapping("/create")
    @Operation(summary = "创建可分享的安全链接（含权限、二维码参数、平台内容）")
    public CommonResult<AppProductShareRespVO> createShare(@Valid @RequestBody AppProductShareCreateReqVO reqVO) {
        ProductShareService.ShareResultDTO dto = productShareService.createShareableLink(
                getLoginUserId(), reqVO.getResourceType(), reqVO.getResourceId(),
                reqVO.getPlatform(), reqVO.getPermissions());
        return success(toVO(dto, null));
    }

    @GetMapping("/content")
    @Operation(summary = "获取平台专属分享内容（微信卡片 / 微博推文）")
    @Parameter(name = "token", description = "分享令牌", required = true)
    @Parameter(name = "platform", description = "目标平台：wechat / weibo / general", required = true)
    @PermitAll
    public CommonResult<Map<String, Object>> getShareContent(@RequestParam("token") String token,
                                                             @RequestParam("platform") String platform) {
        return success(productShareService.generateShareContent(token, platform));
    }

    @GetMapping("/performance")
    @Operation(summary = "获取分享访问统计（ROI 分析）")
    @Parameter(name = "token", description = "分享令牌", required = true)
    public CommonResult<AppProductShareRespVO> getPerformance(@RequestParam("token") String token) {
        Map<String, Object> perf = productShareService.getSharePerformance(token);
        AppProductShareRespVO vo = new AppProductShareRespVO();
        vo.setToken(token);
        vo.setPerformance(perf);
        return success(vo);
    }

    @PostMapping("/verify-access")
    @Operation(summary = "校验分享访问权限（Token + 密码）")
    @Parameter(name = "token", description = "分享令牌", required = true)
    @Parameter(name = "password", description = "访问密码（有密保时必填）")
    @PermitAll
    public CommonResult<Boolean> verifyAccess(@RequestParam("token") String token,
                                               @RequestParam(value = "password", required = false) String password,
                                               @RequestParam(value = "ip", required = false, defaultValue = "0.0.0.0") String ip) {
        return success(productShareService.verifyAccess(token, ip, password));
    }

    @DeleteMapping("/disable")
    @Operation(summary = "停用分享链接")
    @Parameter(name = "token", description = "分享令牌", required = true)
    public CommonResult<Boolean> disableShare(@RequestParam("token") String token) {
        productShareService.disableShare(token, getLoginUserId());
        return success(true);
    }

    private AppProductShareRespVO toVO(ProductShareService.ShareResultDTO dto, Map<String, Object> performance) {
        AppProductShareRespVO vo = new AppProductShareRespVO();
        vo.setToken(dto.token);
        vo.setShareUrl(dto.shareUrl);
        vo.setQrCodeParam(dto.qrCodeParam);
        vo.setExpiresAt(dto.expiresAt);
        vo.setPlatformContent(dto.platformContent);
        vo.setShareButtons(dto.shareButtons);
        vo.setBestTimeToPost(dto.bestTimeToPost);
        return vo;
    }

}
