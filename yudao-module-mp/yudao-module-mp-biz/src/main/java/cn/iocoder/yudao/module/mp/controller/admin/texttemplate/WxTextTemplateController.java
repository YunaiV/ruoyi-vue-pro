package cn.iocoder.yudao.module.mp.controller.admin.texttemplate;

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

import cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.texttemplate.WxTextTemplateDO;
import cn.iocoder.yudao.module.mp.convert.texttemplate.WxTextTemplateConvert;
import cn.iocoder.yudao.module.mp.service.texttemplate.WxTextTemplateService;

@Api(tags = "管理后台 - 文本模板")
@RestController
@RequestMapping("/wechatMp/wx-text-template")
@Validated
public class WxTextTemplateController {

    @Resource
    private WxTextTemplateService wxTextTemplateService;

    @PostMapping("/create")
    @ApiOperation("创建文本模板")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-text-template:create')")
    public CommonResult<Integer> createWxTextTemplate(@Valid @RequestBody WxTextTemplateCreateReqVO createReqVO) {
        return success(wxTextTemplateService.createWxTextTemplate(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新文本模板")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-text-template:update')")
    public CommonResult<Boolean> updateWxTextTemplate(@Valid @RequestBody WxTextTemplateUpdateReqVO updateReqVO) {
        wxTextTemplateService.updateWxTextTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文本模板")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-text-template:delete')")
    public CommonResult<Boolean> deleteWxTextTemplate(@RequestParam("id") Integer id) {
        wxTextTemplateService.deleteWxTextTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得文本模板")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-text-template:query')")
    public CommonResult<WxTextTemplateRespVO> getWxTextTemplate(@RequestParam("id") Integer id) {
        WxTextTemplateDO wxTextTemplate = wxTextTemplateService.getWxTextTemplate(id);
        return success(WxTextTemplateConvert.INSTANCE.convert(wxTextTemplate));
    }

    @GetMapping("/list")
    @ApiOperation("获得文本模板列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-text-template:query')")
    public CommonResult<List<WxTextTemplateRespVO>> getWxTextTemplateList(@RequestParam("ids") Collection<Integer> ids) {
        List<WxTextTemplateDO> list = wxTextTemplateService.getWxTextTemplateList(ids);
        return success(WxTextTemplateConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得文本模板分页")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-text-template:query')")
    public CommonResult<PageResult<WxTextTemplateRespVO>> getWxTextTemplatePage(@Valid WxTextTemplatePageReqVO pageVO) {
        PageResult<WxTextTemplateDO> pageResult = wxTextTemplateService.getWxTextTemplatePage(pageVO);
        return success(WxTextTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出文本模板 Excel")
    @PreAuthorize("@ss.hasPermission('wechatMp:wx-text-template:export')")
    @OperateLog(type = EXPORT)
    public void exportWxTextTemplateExcel(@Valid WxTextTemplateExportReqVO exportReqVO,
                                          HttpServletResponse response) throws IOException {
        List<WxTextTemplateDO> list = wxTextTemplateService.getWxTextTemplateList(exportReqVO);
        // 导出 Excel
        List<WxTextTemplateExcelVO> datas = WxTextTemplateConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "文本模板.xls", "数据", WxTextTemplateExcelVO.class, datas);
    }

}
