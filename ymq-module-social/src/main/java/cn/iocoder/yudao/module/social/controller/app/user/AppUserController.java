package cn.iocoder.yudao.module.social.controller.app.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.social.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.social.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 用户（探针）")
@RestController
@RequestMapping("/social/user")
public class AppUserController {

    @Resource
    private UserService userService;

    /** Sprint 0 探针：模拟微信登录后的用户创建/查询，验证 t_user 表读写。
     *  D5 微信登录链路接好后，此端点废弃。
     */
    @GetMapping("/get-or-create")
    @PermitAll
    @Operation(summary = "按 openid 获取或创建 C 端用户（探针）")
    public CommonResult<UserDO> getOrCreate(@RequestParam("openid") String openid,
                                            @RequestParam(value = "nickname", required = false) String nickname) {
        UserDO user = userService.getOrCreateByOpenid(openid, null, nickname, null);
        return success(user);
    }

}
