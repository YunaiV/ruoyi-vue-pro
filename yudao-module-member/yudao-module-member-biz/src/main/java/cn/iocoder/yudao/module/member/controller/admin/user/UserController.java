package cn.iocoder.yudao.module.member.controller.admin.user;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "用户管理 - 用户管理")
@RestController
@RequestMapping("/member-user")
public class UserController {

    @Resource
    private MemberUserService memberUserService;

    @ApiOperation("获取用户信息")
    @GetMapping("/query-info")
    public CommonResult<MemberUserDO> queryInfo(@PathVariable String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return CommonResult.error(GlobalErrorCodeConstants.BAD_REQUEST.getCode(),"手机号不能为空");
        }
        return CommonResult.success(memberUserService.getUserByMobile(mobile));
    }
}
