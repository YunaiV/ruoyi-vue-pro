package cn.iocoder.yudao.module.mp.controller.admin.fanstag;

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

import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fanstag.WxFansTagDO;
import cn.iocoder.yudao.module.mp.convert.fanstag.WxFansTagConvert;
import cn.iocoder.yudao.module.mp.service.fanstag.WxFansTagService;

@Api(tags = "管理后台 - 粉丝标签")
@RestController
@RequestMapping("/wechatMp/wx-fans-tag")
@Validated
public class WxFansTagController {

    @Resource
    private WxFansTagService wxFansTagService;

    @PostMapping("/create")
    @ApiOperation("创建粉丝标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:create')")
    public CommonResult<Integer> createWxFansTag(@Valid @RequestBody WxFansTagCreateReqVO createReqVO) {
        return success(wxFansTagService.createWxFansTag(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新粉丝标签")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:update')")
    public CommonResult<Boolean> updateWxFansTag(@Valid @RequestBody WxFansTagUpdateReqVO updateReqVO) {
        wxFansTagService.updateWxFansTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除粉丝标签")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:delete')")
    public CommonResult<Boolean> deleteWxFansTag(@RequestParam("id") Integer id) {
        wxFansTagService.deleteWxFansTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得粉丝标签")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:query')")
    public CommonResult<WxFansTagRespVO> getWxFansTag(@RequestParam("id") Integer id) {
        WxFansTagDO wxFansTag = wxFansTagService.getWxFansTag(id);
        return success(WxFansTagConvert.INSTANCE.convert(wxFansTag));
    }

    @GetMapping("/list")
    @ApiOperation("获得粉丝标签列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:query')")
    public CommonResult<List<WxFansTagRespVO>> getWxFansTagList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxFansTagDO> list = wxFansTagService.getWxFansTagList(ids);
        return success(WxFansTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得粉丝标签分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:query')")
    public CommonResult<PageResult<WxFansTagRespVO>> getWxFansTagPage(@Valid WxFansTagPageReqVO pageVO) {
        PageResult<WxFansTagDO> pageResult = wxFansTagService.getWxFansTagPage(pageVO);
        return success(WxFansTagConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出粉丝标签 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-fans-tag:export')")
    @OperateLog(type = EXPORT)
    public void exportWxFansTagExcel(@Valid WxFansTagExportReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        List<WxFansTagDO> list = wxFansTagService.getWxFansTagList(exportReqVO);
        // 导出 Excel
        List<WxFansTagExcelVO> datas = WxFansTagConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "粉丝标签.xls", "数据", WxFansTagExcelVO.class, datas);
    }

}
