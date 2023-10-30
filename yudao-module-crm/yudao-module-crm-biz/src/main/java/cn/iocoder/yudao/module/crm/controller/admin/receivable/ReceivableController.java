package cn.iocoder.yudao.module.crm.controller.admin.receivable;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.convert.receivable.ReceivableConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivableDO;
import cn.iocoder.yudao.module.crm.service.receivable.ReceivableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - CRM 回款管理")
@RestController
@RequestMapping("/crm/receivable")
@Validated
public class ReceivableController {

    @Resource
    private ReceivableService receivableService;

    @PostMapping("/create")
    @Operation(summary = "创建回款管理")
    @PreAuthorize("@ss.hasPermission('crm:receivable:create')")
    public CommonResult<Long> createReceivable(@Valid @RequestBody ReceivableCreateReqVO createReqVO) {
        return success(receivableService.createReceivable(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新回款管理")
    @PreAuthorize("@ss.hasPermission('crm:receivable:update')")
    public CommonResult<Boolean> updateReceivable(@Valid @RequestBody ReceivableUpdateReqVO updateReqVO) {
        receivableService.updateReceivable(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除回款管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:receivable:delete')")
    public CommonResult<Boolean> deleteReceivable(@RequestParam("id") Long id) {
        receivableService.deleteReceivable(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得回款管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:receivable:query')")
    public CommonResult<ReceivableRespVO> getReceivable(@RequestParam("id") Long id) {
        ReceivableDO receivable = receivableService.getReceivable(id);
        return success(ReceivableConvert.INSTANCE.convert(receivable));
    }

    @GetMapping("/page")
    @Operation(summary = "获得回款管理分页")
    @PreAuthorize("@ss.hasPermission('crm:receivable:query')")
    public CommonResult<PageResult<ReceivableRespVO>> getReceivablePage(@Valid ReceivablePageReqVO pageVO) {
        PageResult<ReceivableDO> pageResult = receivableService.getReceivablePage(pageVO);
        return success(ReceivableConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出回款管理 Excel")
    @PreAuthorize("@ss.hasPermission('crm:receivable:export')")
    @OperateLog(type = EXPORT)
    public void exportReceivableExcel(@Valid ReceivableExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ReceivableDO> list = receivableService.getReceivableList(exportReqVO);
        // 导出 Excel
        List<ReceivableExcelVO> datas = ReceivableConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "回款管理.xls", "数据", ReceivableExcelVO.class, datas);
    }

}
