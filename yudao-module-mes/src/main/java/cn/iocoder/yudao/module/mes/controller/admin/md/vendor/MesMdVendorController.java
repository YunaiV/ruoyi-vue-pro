package cn.iocoder.yudao.module.mes.controller.admin.md.vendor;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 供应商")
@RestController
@RequestMapping("/mes/md-vendor")
@Validated
public class MesMdVendorController {

    @Resource
    private MesMdVendorService vendorService;

    @PostMapping("/create")
    @Operation(summary = "创建供应商")
    @PreAuthorize("@ss.hasPermission('mes:md-vendor:create')")
    public CommonResult<Long> createVendor(@Valid @RequestBody MesMdVendorSaveReqVO createReqVO) {
        return success(vendorService.createVendor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新供应商")
    @PreAuthorize("@ss.hasPermission('mes:md-vendor:update')")
    public CommonResult<Boolean> updateVendor(@Valid @RequestBody MesMdVendorSaveReqVO updateReqVO) {
        vendorService.updateVendor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-vendor:delete')")
    public CommonResult<Boolean> deleteVendor(@RequestParam("id") Long id) {
        vendorService.deleteVendor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应商")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:md-vendor:query')")
    public CommonResult<MesMdVendorRespVO> getVendor(@RequestParam("id") Long id) {
        MesMdVendorDO vendor = vendorService.getVendor(id);
        return success(BeanUtils.toBean(vendor, MesMdVendorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应商分页")
    @PreAuthorize("@ss.hasPermission('mes:md-vendor:query')")
    public CommonResult<PageResult<MesMdVendorRespVO>> getVendorPage(@Valid MesMdVendorPageReqVO pageReqVO) {
        PageResult<MesMdVendorDO> pageResult = vendorService.getVendorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesMdVendorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出供应商 Excel")
    @PreAuthorize("@ss.hasPermission('mes:md-vendor:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVendorExcel(@Valid MesMdVendorPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesMdVendorDO> list = vendorService.getVendorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "供应商.xls", "数据", MesMdVendorRespVO.class,
                BeanUtils.toBean(list, MesMdVendorRespVO.class));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得供应商导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<MesMdVendorImportExcelVO> list = Collections.singletonList(
                MesMdVendorImportExcelVO.builder().code("V001").name("示例供应商").nickname("示例")
                        .level("A").telephone("13800138000").status(0).build()
        );
        // 输出
        ExcelUtils.write(response, "供应商导入模板.xls", "供应商列表", MesMdVendorImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入供应商")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新，默认为 false", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('mes:md-vendor:import')")
    public CommonResult<MesMdVendorImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "updateSupport", required = false,
                                                                      defaultValue = "false") Boolean updateSupport) throws Exception {
        List<MesMdVendorImportExcelVO> list = ExcelUtils.read(file, MesMdVendorImportExcelVO.class);
        return success(vendorService.importVendorList(list, updateSupport));
    }

}
