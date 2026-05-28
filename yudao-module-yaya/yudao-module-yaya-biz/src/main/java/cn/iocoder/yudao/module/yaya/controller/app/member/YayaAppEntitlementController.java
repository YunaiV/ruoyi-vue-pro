package cn.iocoder.yudao.module.yaya.controller.app.member;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.app.member.vo.YayaAppEntitlementRespVO;
import cn.iocoder.yudao.module.yaya.service.member.YayaEntitlementService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/yaya/entitlements")
@Validated
public class YayaAppEntitlementController {

    @Resource
    private YayaEntitlementService entitlementService;

    @GetMapping("/me")
    public CommonResult<YayaAppEntitlementRespVO> getMyEntitlement() {
        return success(entitlementService.getMyEntitlement(getLoginUserId()));
    }

}
