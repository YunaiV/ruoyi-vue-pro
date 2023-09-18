package cn.iocoder.yudao.module.member.controller.app.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

// TODO @xiaqing：sign-in
@Tag(name = "签到APP - 签到")
@RestController
@RequestMapping("/member/signin")
public class AppMemberSignInController {

    @Resource
    private MemberSignInRecordService signInRecordService;

    // TODO @xiaqing：泛型：
    // TODO @xiaqing：合并到 AppMemberSignInRecordController 的 getSignInRecordSummary 里哈。
    @Operation(summary = "个人签到信息")
    @GetMapping("/get-summary")
    public CommonResult getUserSummary(){
        return success(signInRecordService.getSignInRecordSummary(getLoginUserId()));
    }

    // TODO @xiaqing：泛型：
    // TODO @xiaqing：合并到 AppMemberSignInRecordController 的 createSignInRecord 里哈。
    @Operation(summary = "会员签到")
    @PostMapping("/create")
    public CommonResult create(){
        MemberSignInRecordDO recordDO = signInRecordService.createSignRecord(getLoginUserId());
        return success(MemberSignInRecordConvert.INSTANCE.coverRecordToAppRecordVo(recordDO));
    }

}
