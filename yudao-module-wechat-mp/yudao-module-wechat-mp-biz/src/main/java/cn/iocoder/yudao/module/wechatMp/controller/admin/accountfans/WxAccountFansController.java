package cn.iocoder.yudao.module.wechatMp.controller.admin.accountfans;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.constraints.*;
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

import cn.iocoder.yudao.module.wechatMp.controller.admin.accountfans.vo.*;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.accountfans.WxAccountFansDO;
import cn.iocoder.yudao.module.wechatMp.convert.accountfans.WxAccountFansConvert;
import cn.iocoder.yudao.module.wechatMp.service.accountfans.WxAccountFansService;

@Api(tags = "管理后台 - 微信公众号粉丝")
@RestController
@RequestMapping("/wechatMp/wx-account-fans")
@Validated
public class WxAccountFansController {

    @Resource
    private WxAccountFansService wxAccountFansService;

    @PostMapping("/create")
    @ApiOperation("创建微信公众号粉丝")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans:create')")
    public CommonResult<Long> createWxAccountFans(@Valid @RequestBody WxAccountFansCreateReqVO createReqVO) {
        return success(wxAccountFansService.createWxAccountFans(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新微信公众号粉丝")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans:update')")
    public CommonResult<Boolean> updateWxAccountFans(@Valid @RequestBody WxAccountFansUpdateReqVO updateReqVO) {
        wxAccountFansService.updateWxAccountFans(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除微信公众号粉丝")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans:delete')")
    public CommonResult<Boolean> deleteWxAccountFans(@RequestParam("id") Long id) {
        wxAccountFansService.deleteWxAccountFans(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得微信公众号粉丝")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans:query')")
    public CommonResult<WxAccountFansRespVO> getWxAccountFans(@RequestParam("id") Long id) {
        WxAccountFansDO wxAccountFans = wxAccountFansService.getWxAccountFans(id);
        return success(WxAccountFansConvert.INSTANCE.convert(wxAccountFans));
    }

    @GetMapping("/list")
    @ApiOperation("获得微信公众号粉丝列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans:query')")
    public CommonResult<List<WxAccountFansRespVO>> getWxAccountFansList(@RequestParam("ids") Collection<Long> ids) {
        List<WxAccountFansDO> list = wxAccountFansService.getWxAccountFansList(ids);
        return success(WxAccountFansConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得微信公众号粉丝分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans:query')")
    public CommonResult<PageResult<WxAccountFansRespVO>> getWxAccountFansPage(@Valid WxAccountFansPageReqVO pageVO) {
        PageResult<WxAccountFansDO> pageResult = wxAccountFansService.getWxAccountFansPage(pageVO);
        return success(WxAccountFansConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出微信公众号粉丝 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans:export')")
    @OperateLog(type = EXPORT)
    public void exportWxAccountFansExcel(@Valid WxAccountFansExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<WxAccountFansDO> list = wxAccountFansService.getWxAccountFansList(exportReqVO);
        // 导出 Excel
        List<WxAccountFansExcelVO> datas = WxAccountFansConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "微信公众号粉丝.xls", "数据", WxAccountFansExcelVO.class, datas);
    }

}
