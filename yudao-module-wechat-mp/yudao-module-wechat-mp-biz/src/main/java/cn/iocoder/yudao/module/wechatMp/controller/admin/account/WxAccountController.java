package cn.iocoder.yudao.module.wechatMp.controller.admin.account;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;

import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.wechatMp.controller.admin.account.vo.*;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.wechatMp.convert.account.WxAccountConvert;
import cn.iocoder.yudao.module.wechatMp.service.account.WxAccountService;

@Api(tags = "管理后台 - 公众号账户")
@RestController
@RequestMapping("/wechatMp/wx-account")
@Validated
public class WxAccountController {

    @Resource
    private WxAccountService wxAccountService;

    @PostMapping("/create")
    @ApiOperation("创建公众号账户")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account:create')")
    public CommonResult<Long> createWxAccount(@Valid @RequestBody WxAccountCreateReqVO createReqVO) {
        return success(wxAccountService.createWxAccount(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新公众号账户")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account:update')")
    public CommonResult<Boolean> updateWxAccount(@Valid @RequestBody WxAccountUpdateReqVO updateReqVO) {
        wxAccountService.updateWxAccount(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除公众号账户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account:delete')")
    public CommonResult<Boolean> deleteWxAccount(@RequestParam("id") Long id) {
        wxAccountService.deleteWxAccount(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得公众号账户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account:query')")
    public CommonResult<WxAccountRespVO> getWxAccount(@RequestParam("id") Long id) {
        WxAccountDO wxAccount = wxAccountService.getWxAccount(id);
        return success(WxAccountConvert.INSTANCE.convert(wxAccount));
    }

    @GetMapping("/list")
    @ApiOperation("获得公众号账户列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account:query')")
    public CommonResult<List<WxAccountRespVO>> getWxAccountList(@RequestParam("ids") Collection<Long> ids) {
        List<WxAccountDO> list = wxAccountService.getWxAccountList(ids);
        return success(WxAccountConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得公众号账户分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account:query')")
    public CommonResult<PageResult<WxAccountRespVO>> getWxAccountPage(@Valid WxAccountPageReqVO pageVO) {
        PageResult<WxAccountDO> pageResult = wxAccountService.getWxAccountPage(pageVO);
        return success(WxAccountConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出公众号账户 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account:export')")
    @OperateLog(type = EXPORT)
    public void exportWxAccountExcel(@Valid WxAccountExportReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        List<WxAccountDO> list = wxAccountService.getWxAccountList(exportReqVO);
        // 导出 Excel
        List<WxAccountExcelVO> datas = WxAccountConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "公众号账户.xls", "数据", WxAccountExcelVO.class, datas);
    }

}
