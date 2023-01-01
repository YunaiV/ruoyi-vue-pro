package cn.iocoder.yudao.module.mp.controller.admin.account;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.*;
import cn.iocoder.yudao.module.mp.convert.account.MpAccountConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.service.account.WxAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 公众号账户")
@RestController
@RequestMapping("/mp/account")
@Validated
public class MpAccountController {

    @Resource
    private WxAccountService wxAccountService;

    @PostMapping("/create")
    @ApiOperation("创建公众号账户")
    @PreAuthorize("@ss.hasPermission('mp:account:create')")
    public CommonResult<Long> createWxAccount(@Valid @RequestBody MpAccountCreateReqVO createReqVO) {
        return success(wxAccountService.createAccount(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新公众号账户")
    @PreAuthorize("@ss.hasPermission('mp:account:update')")
    public CommonResult<Boolean> updateWxAccount(@Valid @RequestBody MpAccountUpdateReqVO updateReqVO) {
        wxAccountService.updateAccount(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除公众号账户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('mp:account:delete')")
    public CommonResult<Boolean> deleteWxAccount(@RequestParam("id") Long id) {
        wxAccountService.deleteAccount(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得公众号账户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('mp:account:query')")
    public CommonResult<MpAccountRespVO> getWxAccount(@RequestParam("id") Long id) {
        MpAccountDO wxAccount = wxAccountService.getAccount(id);
        return success(MpAccountConvert.INSTANCE.convert(wxAccount));
    }

    @GetMapping("/page")
    @ApiOperation("获得公众号账户分页")
    @PreAuthorize("@ss.hasPermission('mp:account:query')")
    public CommonResult<PageResult<MpAccountRespVO>> getWxAccountPage(@Valid MpAccountPageReqVO pageVO) {
        PageResult<MpAccountDO> pageResult = wxAccountService.getAccountPage(pageVO);
        return success(MpAccountConvert.INSTANCE.convertPage(pageResult));
    }

}
