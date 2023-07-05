package cn.iocoder.yudao.module.member.controller.admin.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.service.signin.MemberSignInRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 用户签到积分")
@RestController
@RequestMapping("/point/sign-in-record")
@Validated
public class MemberSignInRecordController {

    @Resource
    private MemberSignInRecordService memberSignInRecordService;

    // TODO @xiaqing：签到是不是不用删除？
    @DeleteMapping("/delete")
    @Operation(summary = "删除用户签到积分")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:delete')")
    public CommonResult<Boolean> deleteSignInRecord(@RequestParam("id") Long id) {
        memberSignInRecordService.deleteSignInRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户签到积分")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<MemberSignInRecordRespVO> getSignInRecord(@RequestParam("id") Long id) {
        MemberSignInRecordDO signInRecord = memberSignInRecordService.getSignInRecord(id);
        return success(MemberSignInRecordConvert.INSTANCE.convert(signInRecord));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户签到积分分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<PageResult<MemberSignInRecordRespVO>> getSignInRecordPage(@Valid MemberSignInRecordPageReqVO pageVO) {
        PageResult<MemberSignInRecordDO> pageResult = memberSignInRecordService.getSignInRecordPage(pageVO);
        return success(MemberSignInRecordConvert.INSTANCE.convertPage(pageResult));
    }
}
