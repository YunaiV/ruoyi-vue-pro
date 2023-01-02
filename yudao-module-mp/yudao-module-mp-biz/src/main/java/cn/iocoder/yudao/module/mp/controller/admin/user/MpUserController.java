package cn.iocoder.yudao.module.mp.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.mp.convert.user.MpUserConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.service.user.MpUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 微信公众号粉丝")
@RestController
@RequestMapping("/mp/user")
@Validated
public class MpUserController {

    @Resource
    private MpUserService mpUserService;

    @GetMapping("/page")
    @ApiOperation("获得微信公众号粉丝分页")
    @PreAuthorize("@ss.hasPermission('mp:user:query')")
    public CommonResult<PageResult<MpUserRespVO>> getUserPage(@Valid MpUserPageReqVO pageVO) {
        PageResult<MpUserDO> pageResult = mpUserService.getUserPage(pageVO);
        return success(MpUserConvert.INSTANCE.convertPage(pageResult));
    }

}
