package cn.iocoder.yudao.module.wms.controller.admin.external.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import cn.iocoder.yudao.module.wms.service.external.storage.WmsExternalStorageService;

@Tag(name = "外部存储库")
@RestController
@RequestMapping("/wms/external-storage")
@Validated
public class WmsExternalStorageController {

    @PostConstruct
    void init() {
        // 原子操作，初始化字典数据
        System.out.println();
    }

    @Resource
    private WmsExternalStorageService externalStorageService;

    @PostMapping("/create")
    @Operation(summary = "创建外部存储库")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:create')")
    public CommonResult<Long> createExternalStorage(@Valid @RequestBody WmsExternalStorageSaveReqVO createReqVO) {
        return success(externalStorageService.createExternalStorage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新外部存储库")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:update')")
    public CommonResult<Boolean> updateExternalStorage(@Valid @RequestBody WmsExternalStorageSaveReqVO updateReqVO) {
        externalStorageService.updateExternalStorage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除外部存储库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:external-storage:delete')")
    public CommonResult<Boolean> deleteExternalStorage(@RequestParam("id") Long id) {
        externalStorageService.deleteExternalStorage(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得外部存储库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:query')")
    public CommonResult<WmsExternalStorageRespVO> getExternalStorage(@RequestParam("id") Long id) {
        WmsExternalStorageDO externalStorage = externalStorageService.getExternalStorage(id);
        return success(BeanUtils.toBean(externalStorage, WmsExternalStorageRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得外部存储库分页")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:query')")
    public CommonResult<PageResult<WmsExternalStorageRespVO>> getExternalStoragePage(@Valid WmsExternalStoragePageReqVO pageReqVO) {
        PageResult<WmsExternalStorageDO> pageResult = externalStorageService.getExternalStoragePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsExternalStorageRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出外部存储库 Excel")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExternalStorageExcel(@Valid WmsExternalStoragePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsExternalStorageDO> list = externalStorageService.getExternalStoragePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "外部存储库.xls", "数据", WmsExternalStorageRespVO.class, BeanUtils.toBean(list, WmsExternalStorageRespVO.class));
    }
}
