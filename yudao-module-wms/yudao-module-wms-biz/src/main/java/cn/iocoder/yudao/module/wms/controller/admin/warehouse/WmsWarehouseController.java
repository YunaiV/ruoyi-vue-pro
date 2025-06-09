package cn.iocoder.yudao.module.wms.controller.admin.warehouse;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.WAREHOUSE_NOT_EXISTS;

/**
 * @author jisencai
 */
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
			.mapping(WmsWarehouseRespVO::getUpdater, WmsWarehouseRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(warehouseVO);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得仓库精简列表")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<List<WmsWarehouseSimpleRespVO>> getWarehouseSimpleList(@Valid WmsWarehousePageReqVO pageReqVO) {
        // 查询数据
        List<WmsWarehouseDO> doList = warehouseService.getSimpleList(pageReqVO);
        // 转换
        List<WmsWarehouseSimpleRespVO> voList = BeanUtils.toBean(doList, WmsWarehouseSimpleRespVO.class);
        // 返回
        return success(voList);
    }

    /**
     * @sign : 225F7C4E91ACF511
     */
    @PostMapping("/page")
    @Operation(summary = "获得仓库分页")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<PageResult<WmsWarehouseRespVO>> getWarehousePage(@Valid @RequestBody WmsWarehousePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsWarehouseDO> doPageResult = warehouseService.getWarehousePage(pageReqVO);
        // 转换
        PageResult<WmsWarehouseRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsWarehouseRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsWarehouseRespVO::getCreator, WmsWarehouseRespVO::setCreatorName)
			.mapping(WmsWarehouseRespVO::getUpdater, WmsWarehouseRespVO::setUpdaterName)
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

    @GetMapping("/simple-list/exchange")
    @Operation(summary = "获得转换单仓库列表")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<List<WmsWarehouseSimpleRespVO>> getWarehouseSimpleListForExchange(Integer exchange) {
        // 查询数据
        List<WmsWarehouseDO> doList = warehouseService.getSimpleListForExchange(exchange);
        // 转换
        List<WmsWarehouseSimpleRespVO> voList = BeanUtils.toBean(doList, WmsWarehouseSimpleRespVO.class);
        // 返回
        return success(voList);
    }
}
