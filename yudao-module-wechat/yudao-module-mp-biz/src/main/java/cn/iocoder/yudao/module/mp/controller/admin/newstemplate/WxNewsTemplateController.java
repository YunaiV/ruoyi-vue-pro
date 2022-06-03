package cn.iocoder.yudao.module.mp.controller.admin.newstemplate;

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

import cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newstemplate.WxNewsTemplateDO;
import cn.iocoder.yudao.module.mp.convert.newstemplate.WxNewsTemplateConvert;
import cn.iocoder.yudao.module.mp.service.newstemplate.WxNewsTemplateService;

@Api(tags = "管理后台 - 图文消息模板")
@RestController
@RequestMapping("/wechatMp/wx-news-template")
@Validated
public class WxNewsTemplateController {

    @Resource
    private WxNewsTemplateService wxNewsTemplateService;

    @PostMapping("/create")
    @ApiOperation("创建图文消息模板")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-template:create')")
    public CommonResult<Integer> createWxNewsTemplate(@Valid @RequestBody WxNewsTemplateCreateReqVO createReqVO) {
        return success(wxNewsTemplateService.createWxNewsTemplate(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新图文消息模板")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-template:update')")
    public CommonResult<Boolean> updateWxNewsTemplate(@Valid @RequestBody WxNewsTemplateUpdateReqVO updateReqVO) {
        wxNewsTemplateService.updateWxNewsTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除图文消息模板")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-template:delete')")
    public CommonResult<Boolean> deleteWxNewsTemplate(@RequestParam("id") Integer id) {
        wxNewsTemplateService.deleteWxNewsTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得图文消息模板")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-template:query')")
    public CommonResult<WxNewsTemplateRespVO> getWxNewsTemplate(@RequestParam("id") Integer id) {
        WxNewsTemplateDO wxNewsTemplate = wxNewsTemplateService.getWxNewsTemplate(id);
        return success(WxNewsTemplateConvert.INSTANCE.convert(wxNewsTemplate));
    }

    @GetMapping("/list")
    @ApiOperation("获得图文消息模板列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-template:query')")
    public CommonResult<List<WxNewsTemplateRespVO>> getWxNewsTemplateList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxNewsTemplateDO> list = wxNewsTemplateService.getWxNewsTemplateList(ids);
        return success(WxNewsTemplateConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得图文消息模板分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-template:query')")
    public CommonResult<PageResult<WxNewsTemplateRespVO>> getWxNewsTemplatePage(@Valid WxNewsTemplatePageReqVO pageVO) {
        PageResult<WxNewsTemplateDO> pageResult = wxNewsTemplateService.getWxNewsTemplatePage(pageVO);
        return success(WxNewsTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出图文消息模板 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-news-template:export')")
    @OperateLog(type = EXPORT)
    public void exportWxNewsTemplateExcel(@Valid WxNewsTemplateExportReqVO exportReqVO,
                                          HttpServletResponse response) throws IOException {
        List<WxNewsTemplateDO> list = wxNewsTemplateService.getWxNewsTemplateList(exportReqVO);
        // 导出 Excel
        List<WxNewsTemplateExcelVO> datas = WxNewsTemplateConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "图文消息模板.xls", "数据", WxNewsTemplateExcelVO.class, datas);
    }

}
