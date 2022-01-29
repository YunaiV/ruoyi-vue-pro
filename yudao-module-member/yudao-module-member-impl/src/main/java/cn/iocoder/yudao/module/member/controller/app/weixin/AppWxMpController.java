package cn.iocoder.yudao.module.member.controller.app.weixin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "微信公众号")
@RestController
@RequestMapping("/member/wx-mp")
@Validated
@Slf4j
public class AppWxMpController {

    @Resource
    private WxMpService mpService;

    @PostMapping("/create-jsapi-signature")
    @ApiOperation(value = "创建微信 JS SDK 初始化所需的签名",
        notes = "参考 https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html 文档")
    public CommonResult<WxJsapiSignature> createJsapiSignature(@RequestParam("url") String url) throws WxErrorException {
        return success(mpService.createJsapiSignature(url));
    }

}
