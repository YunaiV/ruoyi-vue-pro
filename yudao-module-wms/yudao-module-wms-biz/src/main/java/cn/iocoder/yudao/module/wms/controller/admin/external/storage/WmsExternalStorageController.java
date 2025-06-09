package cn.iocoder.yudao.module.wms.controller.admin.external.storage;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.WmsExternalStoragePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.WmsExternalStorageRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.WmsExternalStorageSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import cn.iocoder.yudao.module.wms.service.external.storage.WmsExternalStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.EXTERNAL_STORAGE_NOT_EXISTS;

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

    /**
     * @sign : A4B547533C34EDF4
     */
    @PostMapping("/create")
    @Operation(summary = "创建外部存储库")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:create')")
    public CommonResult<Long> createExternalStorage(@Valid @RequestBody WmsExternalStorageSaveReqVO createReqVO) {
        return success(externalStorageService.createExternalStorage(createReqVO).getId());
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

    /**
     * @sign : 3D13CF2CE3E4C6AB
     */
    @GetMapping("/get")
    @Operation(summary = "获得外部存储库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:query')")
    public CommonResult<WmsExternalStorageRespVO> getExternalStorage(@RequestParam("id") Long id) {
        // 查询数据
        WmsExternalStorageDO externalStorage = externalStorageService.getExternalStorage(id);
        if (externalStorage == null) {
            throw exception(EXTERNAL_STORAGE_NOT_EXISTS);
        }
        // 转换
        WmsExternalStorageRespVO externalStorageVO = BeanUtils.toBean(externalStorage, WmsExternalStorageRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(externalStorageVO))
			.mapping(WmsExternalStorageRespVO::getCreator, WmsExternalStorageRespVO::setCreatorName)
			.mapping(WmsExternalStorageRespVO::getUpdater, WmsExternalStorageRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(externalStorageVO);
    }

    /**
     * @sign : 45E0FA4503BD8C70
     */
    @GetMapping("/page")
    @Operation(summary = "获得外部存储库分页")
    @PreAuthorize("@ss.hasPermission('wms:external-storage:query')")
    public CommonResult<PageResult<WmsExternalStorageRespVO>> getExternalStoragePage(@Valid WmsExternalStoragePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsExternalStorageDO> doPageResult = externalStorageService.getExternalStoragePage(pageReqVO);
        // 转换
        PageResult<WmsExternalStorageRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsExternalStorageRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsExternalStorageRespVO::getCreator, WmsExternalStorageRespVO::setCreatorName)
			.mapping(WmsExternalStorageRespVO::getUpdater, WmsExternalStorageRespVO::setUpdaterName)
			.fill();
        // 清除不需要的返回值
        voPageResult.getList().forEach(item -> {
            item.setApiParameters(null);
        });
        // 返回
        return success(voPageResult);
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
