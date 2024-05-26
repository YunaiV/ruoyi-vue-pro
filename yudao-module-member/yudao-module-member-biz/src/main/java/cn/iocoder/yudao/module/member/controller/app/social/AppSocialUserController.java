package cn.iocoder.yudao.module.member.controller.app.social;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.controller.app.social.vo.AppSocialUserBindReqVO;
import cn.iocoder.yudao.module.member.controller.app.social.vo.AppSocialUserRespVO;
import cn.iocoder.yudao.module.member.controller.app.social.vo.AppSocialUserUnbindReqVO;
import cn.iocoder.yudao.module.member.controller.app.social.vo.AppSocialWxQrcodeReqVO;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserUnbindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 社交用户")
@RestController
@RequestMapping("/member/social-user")
@Validated
public class AppSocialUserController {

    public static final String ENV_VERSION = "release"; // 小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"
    private static final String SCENE = ""; // 页面路径不能携带参数（参数请放在scene字段里）
    private static final Integer WIDTH = 430; // 二维码宽度
    private static final Boolean AUTO_COLOR = true; // 默认true 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
    private static final Boolean CHECK_PATH = true; // 默认true 检查 page 是否存在
    private static final Boolean HYALINE = true; // 是否需要透明底色， hyaline 为true时，生成透明底色的小程序码

    @Resource
    private SocialUserApi socialUserApi;
    @Resource
    private SocialClientApi socialClientApi;

    @PostMapping("/bind")
    @Operation(summary = "社交绑定，使用 code 授权码")
    public CommonResult<String> socialBind(@RequestBody @Valid AppSocialUserBindReqVO reqVO) {
        SocialUserBindReqDTO reqDTO = new SocialUserBindReqDTO(getLoginUserId(), UserTypeEnum.MEMBER.getValue(),
                reqVO.getType(), reqVO.getCode(), reqVO.getState());
        String openid = socialUserApi.bindSocialUser(reqDTO);
        return success(openid);
    }

    @DeleteMapping("/unbind")
    @Operation(summary = "取消社交绑定")
    @PreAuthenticated
    public CommonResult<Boolean> socialUnbind(@RequestBody AppSocialUserUnbindReqVO reqVO) {
        SocialUserUnbindReqDTO reqDTO = new SocialUserUnbindReqDTO(getLoginUserId(), UserTypeEnum.MEMBER.getValue(),
                reqVO.getType(), reqVO.getOpenid());
        socialUserApi.unbindSocialUser(reqDTO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得社交用户")
    @Parameter(name = "type", description = "社交平台的类型，参见 SocialTypeEnum 枚举值", required = true, example = "10")
    @PreAuthenticated
    public CommonResult<AppSocialUserRespVO> getSocialUser(@RequestParam("type") Integer type) {
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(UserTypeEnum.MEMBER.getValue(), getLoginUserId(), type);
        return success(BeanUtils.toBean(socialUser, AppSocialUserRespVO.class));
    }

    @PostMapping("/wxa-qrcode")
    @Operation(summary = "获得微信小程序码(base64 image)")
    public CommonResult<String> getWxQrcode(@RequestBody @Valid AppSocialWxQrcodeReqVO reqVO) {
        byte[] wxQrcode = socialClientApi.getWxaQrcode(new SocialWxQrcodeReqDTO().setPath(reqVO.getPath())
                .setEnvVersion(ENV_VERSION).setWidth(ObjUtil.defaultIfNull(reqVO.getWidth(), WIDTH))
                .setScene(ObjUtil.defaultIfNull(reqVO.getScene(), SCENE))
                .setAutoColor(ObjUtil.defaultIfNull(reqVO.getAutoColor(), AUTO_COLOR))
                .setHyaline(ObjUtil.defaultIfNull(reqVO.getHyaline(), HYALINE))
                .setCheckPath(ObjUtil.defaultIfNull(reqVO.getCheckPath(), CHECK_PATH)));
        return success(Base64.encode(wxQrcode));
    }

}
