package cn.iocoder.yudao.adminserver.modules.activiti.controller.form;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.*;
import cn.iocoder.yudao.adminserver.modules.activiti.convert.form.WfFormConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.WfForm;
import cn.iocoder.yudao.adminserver.modules.activiti.service.form.WfFormService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

// TODO @风里雾里： Os=》Wf，/os 改成 /wl 开头。目前这个模块，咱先定位成给工作流用的
@Api(tags = "动态表单")
@RestController
@RequestMapping("/wl/form")
@Validated
public class WlFormController {

    @Resource
    private WfFormService formService;

    @PostMapping("/create")
    @ApiOperation("创建动态表单")
    @PreAuthorize("@ss.hasPermission('os:form:create')")
    public CommonResult<Long> createForm(@Valid @RequestBody WfFormCreateReqVO createReqVO) {
        return success(formService.createForm(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新动态表单")
    @PreAuthorize("@ss.hasPermission('os:form:update')")
    public CommonResult<Boolean> updateForm(@Valid @RequestBody WfFormUpdateReqVO updateReqVO) {
        formService.updateForm(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除动态表单")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('os:form:delete')")
    public CommonResult<Boolean> deleteForm(@RequestParam("id") Long id) {
        formService.deleteForm(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得动态表单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('os:form:query')")
    public CommonResult<WfFormRespVO> getForm(@RequestParam("id") Long id) {
        WfForm form = formService.getForm(id);
        return success(WfFormConvert.INSTANCE.convert(form));
    }

    @GetMapping("/list")
    @ApiOperation("获得动态表单列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('os:form:query')")
    public CommonResult<List<WfFormRespVO>> getFormList(@RequestParam("ids") Collection<Long> ids) {
        List<WfForm> list = formService.getFormList(ids);
        return success(WfFormConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得动态表单分页")
    @PreAuthorize("@ss.hasPermission('os:form:query')")
    public CommonResult<PageResult<WfFormRespVO>> getFormPage(@Valid WfFormPageReqVO pageVO) {
        PageResult<WfForm> pageResult = formService.getFormPage(pageVO);
        return success(WfFormConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出动态表单 Excel")
    @PreAuthorize("@ss.hasPermission('os:form:export')")
    @OperateLog(type = EXPORT)
    public void exportFormExcel(@Valid WfFormExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<WfForm> list = formService.getFormList(exportReqVO);
        // 导出 Excel
        List<WfFormExcelVO> datas = WfFormConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "动态表单.xls", "数据", WfFormExcelVO.class, datas);
    }

}
