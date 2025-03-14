package cn.iocoder.yudao.module.wms.controller.admin.warehouse.area;

import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.context.annotation.Lazy;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_AREA_NOT_EXISTS;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.area.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.area.WmsWarehouseAreaDO;
import cn.iocoder.yudao.module.wms.service.warehouse.area.WmsWarehouseAreaService;

@Tag(name = "库区")
@RestController
@RequestMapping("/wms/warehouse-area")
@Validated
public class WmsWarehouseAreaController {

    @Resource
    private WmsWarehouseAreaService warehouseAreaService;

    /**
     * @sign : DBD56AF8A21F3782
     */
    @PostMapping("/create")
    @Operation(summary = "创建库区")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-area:create')")
    public CommonResult<Long> createWarehouseArea(@Valid @RequestBody WmsWarehouseAreaSaveReqVO createReqVO) {
        return success(warehouseAreaService.createWarehouseArea(createReqVO).getId());
    }


    @PutMapping("/update")
    @Operation(summary = "更新库区")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-area:update')")
    public CommonResult<Boolean> updateWarehouseArea(@Valid @RequestBody WmsWarehouseAreaSaveReqVO updateReqVO) {
        warehouseAreaService.updateWarehouseArea(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库区")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:warehouse-area:delete')")
    public CommonResult<Boolean> deleteWarehouseArea(@RequestParam("id") Long id) {
        warehouseAreaService.deleteWarehouseArea(id);
        return success(true);
    }

    /**
     * @sign : 642A9D87BC410F83
     */
    @GetMapping("/get")
    @Operation(summary = "获得库区")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-area:query')")
    public CommonResult<WmsWarehouseAreaRespVO> getWarehouseArea(@RequestParam("id") Long id) {
        // 查询数据
        WmsWarehouseAreaDO warehouseArea = warehouseAreaService.getWarehouseArea(id);
        if (warehouseArea == null) {
            throw exception(WAREHOUSE_AREA_NOT_EXISTS);
        }
        // 转换
        WmsWarehouseAreaRespVO warehouseAreaVO = BeanUtils.toBean(warehouseArea, WmsWarehouseAreaRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(warehouseAreaVO))
			.mapping(WmsWarehouseAreaRespVO::getCreator, WmsWarehouseAreaRespVO::setCreatorName)
			.mapping(WmsWarehouseAreaRespVO::getCreator, WmsWarehouseAreaRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(warehouseAreaVO);
    }

    /**
     * @sign : 84916C00BDD924B9
     */
    @GetMapping("/page")
    @Operation(summary = "获得库区分页")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-area:query')")
    public CommonResult<PageResult<WmsWarehouseAreaRespVO>> getWarehouseAreaPage(@Valid WmsWarehouseAreaPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsWarehouseAreaDO> doPageResult = warehouseAreaService.getWarehouseAreaPage(pageReqVO);
        // 转换
        PageResult<WmsWarehouseAreaRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsWarehouseAreaRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsWarehouseAreaRespVO::getCreator, WmsWarehouseAreaRespVO::setCreatorName)
			.mapping(WmsWarehouseAreaRespVO::getCreator, WmsWarehouseAreaRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库区 Excel")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-area:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseAreaExcel(@Valid WmsWarehouseAreaPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsWarehouseAreaDO> list = warehouseAreaService.getWarehouseAreaPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库区.xls", "数据", WmsWarehouseAreaRespVO.class, BeanUtils.toBean(list, WmsWarehouseAreaRespVO.class));
    }
}
