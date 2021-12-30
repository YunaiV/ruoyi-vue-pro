package cn.iocoder.yudao.adminserver.modules.bpm.controller.form;

import cn.iocoder.yudao.adminserver.modules.bpm.convert.form.BpmFormConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmForm;
import cn.iocoder.yudao.adminserver.modules.bpm.service.form.BpmFormService;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.*;
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

@Api(tags = "动态表单")
@RestController
@RequestMapping("/bpm/form")
@Validated
public class BpmFormController {

    @Resource
    private BpmFormService formService;

    @PostMapping("/create")
    @ApiOperation("创建动态表单")
    @PreAuthorize("@ss.hasPermission('bpm:form:create')")
    public CommonResult<Long> createForm(@Valid @RequestBody BpmFormCreateReqVO createReqVO) {
        return success(formService.createForm(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新动态表单")
    @PreAuthorize("@ss.hasPermission('bpm:form:update')")
    public CommonResult<Boolean> updateForm(@Valid @RequestBody BpmFormUpdateReqVO updateReqVO) {
        formService.updateForm(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除动态表单")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:form:delete')")
    public CommonResult<Boolean> deleteForm(@RequestParam("id") Long id) {
        formService.deleteForm(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得动态表单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('bpm:form:query')")
    public CommonResult<BpmFormRespVO> getForm(@RequestParam("id") Long id) {
        BpmForm form = formService.getForm(id);
        return success(BpmFormConvert.INSTANCE.convert(form));
    }

    @GetMapping("/list")
    @ApiOperation("获得动态表单列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('bpm:form:query')")
    public CommonResult<List<BpmFormRespVO>> getFormList(@RequestParam("ids") Collection<Long> ids) {
        List<BpmForm> list = formService.getFormList(ids);
        return success(BpmFormConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得动态表单分页")
    @PreAuthorize("@ss.hasPermission('bpm:form:query')")
    public CommonResult<PageResult<BpmFormRespVO>> getFormPage(@Valid BpmFormPageReqVO pageVO) {
        PageResult<BpmForm> pageResult = formService.getFormPage(pageVO);
        return success(BpmFormConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出动态表单 Excel")
    @PreAuthorize("@ss.hasPermission('bpm:form:export')")
    @OperateLog(type = EXPORT)
    public void exportFormExcel(@Valid BpmFormExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<BpmForm> list = formService.getFormList(exportReqVO);
        // 导出 Excel
        List<BpmFormExcelVO> datas = BpmFormConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "动态表单.xls", "数据", BpmFormExcelVO.class, datas);
    }

}
