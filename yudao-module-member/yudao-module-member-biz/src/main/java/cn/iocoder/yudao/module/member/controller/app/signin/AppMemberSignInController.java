package cn.iocoder.yudao.module.member.controller.app.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.service.signin.MemberSignInRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 描述    :用户签到相关信息接口
 * Author :xiaqing
 * Date   :2023-09-15 09:02
 */
@Tag(name = "签到APP - 签到")
@RestController
@RequestMapping("/member/signin")
public class AppMemberSignInController {

    @Resource
    MemberSignInRecordService signInRecordService;

    /**
     * 描述    :获取个人签到信息
     * Author :xiaqing
     * Date   :2023-09-15 12:56:47
     */

    @Operation(summary = "个人签到信息")
    @GetMapping("/get-summary")
    public CommonResult getUserSummary(){
        return CommonResult.success(signInRecordService.getUserSummary(SecurityFrameworkUtils.getLoginUserId()));
    }


    /**
     * 描述    :用户签到
     * Author :xiaqing
     * Date   :2023-09-15 09:20:58
     */
    @Operation(summary = "会员签到")
    @PostMapping("/create")
    public CommonResult create(){
        MemberSignInRecordDO recordDO = signInRecordService.create(SecurityFrameworkUtils.getLoginUserId());
        return CommonResult.success(MemberSignInRecordConvert.INSTANCE.coverRecordToAppRecordVo(recordDO));
    }

}
