package cn.iocoder.yudao.module.wms.controller.admin.warehouse.location;

import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_LOCATION_NOT_EXISTS;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.location.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.location.WmsWarehouseLocationDO;
import cn.iocoder.yudao.module.wms.service.warehouse.location.WmsWarehouseLocationService;

@Tag(name = "库位")
@RestController
@RequestMapping("/wms/warehouse-location")
@Validated
public class WmsWarehouseLocationController {

    @Resource
    private WmsWarehouseLocationService warehouseLocationService;

    /**
     * @sign : 0E34A87213C18667
     */
    @PostMapping("/create")
    @Operation(summary = "创建库位")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-location:create')")
    public CommonResult<Long> createWarehouseLocation(@Valid @RequestBody WmsWarehouseLocationSaveReqVO createReqVO) {
        return success(warehouseLocationService.createWarehouseLocation(createReqVO).getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新库位")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-location:update')")
    public CommonResult<Boolean> updateWarehouseLocation(@Valid @RequestBody WmsWarehouseLocationSaveReqVO updateReqVO) {
        warehouseLocationService.updateWarehouseLocation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库位")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:warehouse-location:delete')")
    public CommonResult<Boolean> deleteWarehouseLocation(@RequestParam("id") Long id) {
        warehouseLocationService.deleteWarehouseLocation(id);
        return success(true);
    }

    /**
     * @sign : EA016B3403EF28DF
     */
    @GetMapping("/get")
    @Operation(summary = "获得库位")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-location:query')")
    public CommonResult<WmsWarehouseLocationRespVO> getWarehouseLocation(@RequestParam("id") Long id) {
        // 查询数据
        WmsWarehouseLocationDO warehouseLocation = warehouseLocationService.getWarehouseLocation(id);
        if (warehouseLocation == null) {
            throw exception(WAREHOUSE_LOCATION_NOT_EXISTS);
        }
        // 转换
        WmsWarehouseLocationRespVO warehouseLocationVO = BeanUtils.toBean(warehouseLocation, WmsWarehouseLocationRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(warehouseLocationVO))
			.mapping(WmsWarehouseLocationRespVO::getCreator, WmsWarehouseLocationRespVO::setCreatorName)
			.mapping(WmsWarehouseLocationRespVO::getCreator, WmsWarehouseLocationRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(warehouseLocationVO);
    }

    /**
     * @sign : DCCA3492D41700E4
     */
    @GetMapping("/page")
    @Operation(summary = "获得库位分页")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-location:query')")
    public CommonResult<PageResult<WmsWarehouseLocationRespVO>> getWarehouseLocationPage(@Valid WmsWarehouseLocationPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsWarehouseLocationDO> doPageResult = warehouseLocationService.getWarehouseLocationPage(pageReqVO);
        // 转换
        PageResult<WmsWarehouseLocationRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsWarehouseLocationRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsWarehouseLocationRespVO::getCreator, WmsWarehouseLocationRespVO::setCreatorName)
			.mapping(WmsWarehouseLocationRespVO::getCreator, WmsWarehouseLocationRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库位 Excel")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-location:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseLocationExcel(@Valid WmsWarehouseLocationPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsWarehouseLocationDO> list = warehouseLocationService.getWarehouseLocationPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库位.xls", "数据", WmsWarehouseLocationRespVO.class, BeanUtils.toBean(list, WmsWarehouseLocationRespVO.class));
    }
}