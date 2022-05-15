package cn.iocoder.yudao.module.member.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.user.dto.UserInfoDTO;
import cn.iocoder.yudao.module.member.api.user.dto.UserRespDTO;
import cn.iocoder.yudao.module.member.convert.user.UserConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author Banging
 */
@Slf4j
@Api("用户管理")
@RestController(value = "memberUserController")
@RequestMapping("/user")
public class UserController {

    @Resource
    private MemberUserService userService;

    @ApiOperation(value = "用户信息获取",notes = "用户基本信息的获取")
    @GetMapping("/{tel}")
    public CommonResult<UserInfoDTO> getUserInfo(@PathVariable String tel){
        MemberUserDO user = userService.getUserByMobile(tel);
        return CommonResult.success(UserConvert.INSTANCE.convertInfo(user));
    }
}
