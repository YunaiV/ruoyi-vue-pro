package cn.iocoder.yudao.module.member.controller.admin.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.service.signin.MemberSignInRecordService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 用户签到积分")
@RestController
@RequestMapping("/point/sign-in-record")
@Validated
public class MemberSignInRecordController {

    @Resource
    private MemberSignInRecordService memberSignInRecordService;
    @Resource
    AdminUserApi adminUserApi;


    @GetMapping("/page")
    @Operation(summary = "获得用户签到积分分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult <PageResult <MemberSignInRecordRespVO>> getSignInRecordPage(@Valid MemberSignInRecordPageReqVO pageVO) {
        PageResult <MemberSignInRecordDO> pageResult = memberSignInRecordService.getSignInRecordPage(pageVO);
        List <MemberSignInRecordDO> result = pageResult.getList();
        //设置获取的用户信息
        List <AdminUserRespDTO> users = null;
        if (!CollectionUtils.isEmpty(result)) {
            List <Long> ids = result.stream().map(user -> user.getUserId()).collect(Collectors.toList());
            users = adminUserApi.getUserList(ids);
        }
        return success(MemberSignInRecordConvert.INSTANCE.convertPage(pageResult, users));
    }
}
