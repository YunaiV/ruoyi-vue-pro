package cn.iocoder.yudao.module.mp.controller.admin.accountfanstag;

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

import cn.iocoder.yudao.module.mp.controller.admin.accountfanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfanstag.WxAccountFansTagDO;
import cn.iocoder.yudao.module.mp.convert.accountfanstag.WxAccountFansTagConvert;
import cn.iocoder.yudao.module.mp.service.accountfanstag.WxAccountFansTagService;

@Api(tags = "管理后台 - 粉丝标签关联")
@RestController
@RequestMapping("/wechatMp/wx-account-fans-tag")
@Validated
public class WxAccountFansTagController {

    @Resource
    private WxAccountFansTagService wxAccountFansTagService;

    @PostMapping("/create")
    @ApiOperation("创建粉丝标签关联")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans-tag:create')")
    public CommonResult<Integer> createWxAccountFansTag(@Valid @RequestBody WxAccountFansTagCreateReqVO createReqVO) {
        return success(wxAccountFansTagService.createWxAccountFansTag(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新粉丝标签关联")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans-tag:update')")
    public CommonResult<Boolean> updateWxAccountFansTag(@Valid @RequestBody WxAccountFansTagUpdateReqVO updateReqVO) {
        wxAccountFansTagService.updateWxAccountFansTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除粉丝标签关联")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans-tag:delete')")
    public CommonResult<Boolean> deleteWxAccountFansTag(@RequestParam("id") Integer id) {
        wxAccountFansTagService.deleteWxAccountFansTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得粉丝标签关联")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans-tag:query')")
    public CommonResult<WxAccountFansTagRespVO> getWxAccountFansTag(@RequestParam("id") Integer id) {
        WxAccountFansTagDO wxAccountFansTag = wxAccountFansTagService.getWxAccountFansTag(id);
        return success(WxAccountFansTagConvert.INSTANCE.convert(wxAccountFansTag));
    }

    @GetMapping("/list")
    @ApiOperation("获得粉丝标签关联列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans-tag:query')")
    public CommonResult<List<WxAccountFansTagRespVO>> getWxAccountFansTagList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxAccountFansTagDO> list = wxAccountFansTagService.getWxAccountFansTagList(ids);
        return success(WxAccountFansTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得粉丝标签关联分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans-tag:query')")
    public CommonResult<PageResult<WxAccountFansTagRespVO>> getWxAccountFansTagPage(@Valid WxAccountFansTagPageReqVO pageVO) {
        PageResult<WxAccountFansTagDO> pageResult = wxAccountFansTagService.getWxAccountFansTagPage(pageVO);
        return success(WxAccountFansTagConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出粉丝标签关联 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-account-fans-tag:export')")
    @OperateLog(type = EXPORT)
    public void exportWxAccountFansTagExcel(@Valid WxAccountFansTagExportReqVO exportReqVO,
                                            HttpServletResponse response) throws IOException {
        List<WxAccountFansTagDO> list = wxAccountFansTagService.getWxAccountFansTagList(exportReqVO);
        // 导出 Excel
        List<WxAccountFansTagExcelVO> datas = WxAccountFansTagConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "粉丝标签关联.xls", "数据", WxAccountFansTagExcelVO.class, datas);
    }

}
