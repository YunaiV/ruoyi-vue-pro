package cn.iocoder.yudao.module.wms.controller.admin.warehouse;

import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
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
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_NOT_EXISTS;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "仓库")
@RestController
@RequestMapping("/wms/warehouse")
@Validated
public class WmsWarehouseController {

    @Resource
    private WmsWarehouseService warehouseService;



    /**
     * @sign : 85CB8518B4499F29
     */
    @PostMapping("/create")
    @Operation(summary = "创建仓库")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:create')")
    public CommonResult<Long> createWarehouse(@Valid @RequestBody WmsWarehouseSaveReqVO createReqVO) {
        return success(warehouseService.createWarehouse(createReqVO).getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新仓库")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:update')")
    public CommonResult<Boolean> updateWarehouse(@Valid @RequestBody WmsWarehouseSaveReqVO updateReqVO) {
        warehouseService.updateWarehouse(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除仓库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:warehouse:delete')")
    public CommonResult<Boolean> deleteWarehouse(@RequestParam("id") Long id) {
        warehouseService.deleteWarehouse(id);
        return success(true);
    }

    /**
     * @sign : 79646F50B1235FF5
     */
    @GetMapping("/get")
    @Operation(summary = "获得仓库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<WmsWarehouseRespVO> getWarehouse(@RequestParam("id") Long id) {
        // 查询数据
        WmsWarehouseDO warehouse = warehouseService.getWarehouse(id);
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        // 转换
        WmsWarehouseRespVO warehouseVO = BeanUtils.toBean(warehouse, WmsWarehouseRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(warehouseVO))
			.mapping(WmsWarehouseRespVO::getCreator, WmsWarehouseRespVO::setCreatorName)
			.mapping(WmsWarehouseRespVO::getCreator, WmsWarehouseRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(warehouseVO);
    }

    /**
     * @sign : 225F7C4E91ACF511
     */
    @GetMapping("/page")
    @Operation(summary = "获得仓库分页")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<PageResult<WmsWarehouseRespVO>> getWarehousePage(@Valid WmsWarehousePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsWarehouseDO> doPageResult = warehouseService.getWarehousePage(pageReqVO);
        // 转换
        PageResult<WmsWarehouseRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsWarehouseRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsWarehouseRespVO::getCreator, WmsWarehouseRespVO::setCreatorName)
			.mapping(WmsWarehouseRespVO::getCreator, WmsWarehouseRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓库 Excel")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseExcel(@Valid WmsWarehousePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsWarehouseDO> list = warehouseService.getWarehousePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "仓库.xls", "数据", WmsWarehouseRespVO.class, BeanUtils.toBean(list, WmsWarehouseRespVO.class));
    }
}
