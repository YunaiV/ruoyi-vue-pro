package cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZoneSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone.WmsWarehouseZoneDO;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import cn.iocoder.yudao.module.wms.service.warehouse.zone.WmsWarehouseZoneService;
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
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.WAREHOUSE_BIN_NOT_EXISTS;

@Tag(name = "库位")
@RestController
@RequestMapping("/wms/warehouse-bin")
@Validated
public class WmsWarehouseBinController {

    @Resource
    private WmsWarehouseBinService warehouseBinService;

    @Resource
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsWarehouseZoneService warehouseZoneService;

    /**
     * @sign : 9D03CAD0A777558E
     */
    @PostMapping("/create")
    @Operation(summary = "创建库位")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:create')")
    public CommonResult<Long> createWarehouseBin(@Valid @RequestBody WmsWarehouseBinSaveReqVO createReqVO) {
        return success(warehouseBinService.createWarehouseBin(createReqVO).getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新库位")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:update')")
    public CommonResult<Boolean> updateWarehouseBin(@Valid @RequestBody WmsWarehouseBinSaveReqVO updateReqVO) {
        warehouseBinService.updateWarehouseBin(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库位")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:delete')")
    public CommonResult<Boolean> deleteWarehouseBin(@RequestParam("id") Long id) {
        warehouseBinService.deleteWarehouseBin(id);
        return success(true);
    }

    /**
     * @sign : D16F15F6343AB9B1
     */
    @GetMapping("/get")
    @Operation(summary = "获得库位")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:query')")
    public CommonResult<WmsWarehouseBinRespVO> getWarehouseBin(@RequestParam("id") Long id) {
        // 查询数据
        WmsWarehouseBinDO warehouseBin = warehouseBinService.getWarehouseBin(id);
        if (warehouseBin == null) {
            throw exception(WAREHOUSE_BIN_NOT_EXISTS);
        }
        // 转换
        WmsWarehouseBinRespVO warehouseBinVO = BeanUtils.toBean(warehouseBin, WmsWarehouseBinRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(warehouseBinVO))
			.mapping(WmsWarehouseBinRespVO::getCreator, WmsWarehouseBinRespVO::setCreatorName)
			.mapping(WmsWarehouseBinRespVO::getUpdater, WmsWarehouseBinRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(warehouseBinVO);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得库位精简列表")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:query')")
    public CommonResult<List<WmsWarehouseBinSimpleRespVO>> getWarehouseZoneSimpleList(@Valid WmsWarehouseBinPageReqVO pageReqVO) {
        // 查询数据
        List<WmsWarehouseBinDO> doList = warehouseBinService.getSimpleList(pageReqVO);
        // 转换
        List<WmsWarehouseBinSimpleRespVO> voList = BeanUtils.toBean(doList, WmsWarehouseBinSimpleRespVO.class);

        return success(voList);
    }

    /**
     * @sign : AD24BD1BAD790208
     */
    @GetMapping("/page")
    @Operation(summary = "获得库位分页")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:query')")
    public CommonResult<PageResult<WmsWarehouseBinRespVO>> getWarehouseBinPage(@Valid WmsWarehouseBinPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsWarehouseBinDO> doPageResult = warehouseBinService.getWarehouseBinPage(pageReqVO);
        // 转换
        PageResult<WmsWarehouseBinRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsWarehouseBinRespVO.class);
        // 装配仓库
        List<WmsWarehouseDO> warehouseDOList = warehouseService.selectByIds(StreamX.from(voPageResult.getList()).toList(WmsWarehouseBinRespVO::getWarehouseId));
        StreamX.from(voPageResult.getList()).assemble(warehouseDOList, WmsWarehouseDO::getId, WmsWarehouseBinRespVO::getWarehouseId, (b,w)->{
            b.setWarehouse(BeanUtils.toBean(w, WmsWarehouseSimpleRespVO.class));
        });
        // 装配库区
        List<WmsWarehouseZoneDO> warehouseZoneDOList = warehouseZoneService.selectByIds(StreamX.from(voPageResult.getList()).toSet(WmsWarehouseBinRespVO::getZoneId));
        StreamX.from(voPageResult.getList()).assemble(warehouseZoneDOList, WmsWarehouseZoneDO::getId, WmsWarehouseBinRespVO::getZoneId, (b,w)->{
            b.setZone(BeanUtils.toBean(w, WmsWarehouseZoneSimpleRespVO.class));
        });
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsWarehouseBinRespVO::getCreator, WmsWarehouseBinRespVO::setCreatorName)
			.mapping(WmsWarehouseBinRespVO::getUpdater, WmsWarehouseBinRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库位 Excel")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseBinExcel(@Valid WmsWarehouseBinPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsWarehouseBinDO> list = warehouseBinService.getWarehouseBinPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库位.xls", "数据", WmsWarehouseBinRespVO.class, BeanUtils.toBean(list, WmsWarehouseBinRespVO.class));
    }

    @GetMapping("/exchange/simple-list")
    @Operation(summary = "获得转换单库位精简列表")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-bin:query')")
    public CommonResult<List<WmsWarehouseBinSimpleRespVO>> getWarehouseSimpleListForExchange(@Valid WmsWarehouseBinPageReqVO pageReqVO) {
        // 查询数据
        List<WmsWarehouseBinDO> doList = warehouseBinService.getSimpleListForExchange(pageReqVO);
        // 转换
        List<WmsWarehouseBinSimpleRespVO> voList = BeanUtils.toBean(doList, WmsWarehouseBinSimpleRespVO.class);

        return success(voList);
    }
}
