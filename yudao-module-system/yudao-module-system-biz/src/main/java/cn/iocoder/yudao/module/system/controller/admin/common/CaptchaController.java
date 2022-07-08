package cn.iocoder.yudao.module.system.controller.admin.common;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.controller.admin.common.vo.CaptchaImageRespVO;
import cn.iocoder.yudao.module.system.service.common.CaptchaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 验证码")
@RestController
@RequestMapping("/system/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    @GetMapping("/get-image")
    @PermitAll
    @ApiOperation("生成图片验证码")
    public CommonResult<CaptchaImageRespVO> getCaptchaImage() {
        return success(captchaService.getCaptchaImage());
    }

}
