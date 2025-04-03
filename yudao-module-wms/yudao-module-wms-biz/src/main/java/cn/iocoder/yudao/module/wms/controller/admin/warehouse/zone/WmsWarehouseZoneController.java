package cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZonePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZoneRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZoneSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZoneSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone.WmsWarehouseZoneDO;
import cn.iocoder.yudao.module.wms.service.warehouse.zone.WmsWarehouseZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.WAREHOUSE_ZONE_NOT_EXISTS;

@Tag(name = "库区")
@RestController
@RequestMapping("/wms/warehouse-zone")
@Validated
public class WmsWarehouseZoneController {

    @Resource
    private WmsWarehouseZoneService warehouseZoneService;

    /**
     * @sign : 3414BB55B7829714
     */
    @PostMapping("/create")
    @Operation(summary = "创建库区")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-zone:create')")
    public CommonResult<Long> createWarehouseZone(@Valid @RequestBody WmsWarehouseZoneSaveReqVO createReqVO) {
        return success(warehouseZoneService.createWarehouseZone(createReqVO).getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新库区")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-zone:update')")
    public CommonResult<Boolean> updateWarehouseZone(@Valid @RequestBody WmsWarehouseZoneSaveReqVO updateReqVO) {
        warehouseZoneService.updateWarehouseZone(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库区")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:warehouse-zone:delete')")
    public CommonResult<Boolean> deleteWarehouseZone(@RequestParam("id") Long id) {
        warehouseZoneService.deleteWarehouseZone(id);
        return success(true);
    }

    /**
     * @sign : F379D4E86E76FC27
     */
    @GetMapping("/get")
    @Operation(summary = "获得库区")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-zone:query')")
    public CommonResult<WmsWarehouseZoneRespVO> getWarehouseZone(@RequestParam("id") Long id) {
        // 查询数据
        WmsWarehouseZoneDO warehouseZone = warehouseZoneService.getWarehouseZone(id);
        if (warehouseZone == null) {
            throw exception(WAREHOUSE_ZONE_NOT_EXISTS);
        }
        // 转换
        WmsWarehouseZoneRespVO warehouseZoneVO = BeanUtils.toBean(warehouseZone, WmsWarehouseZoneRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(warehouseZoneVO))
			.mapping(WmsWarehouseZoneRespVO::getCreator, WmsWarehouseZoneRespVO::setCreatorName)
			.mapping(WmsWarehouseZoneRespVO::getCreator, WmsWarehouseZoneRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(warehouseZoneVO);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得库区精简列表")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-zone:query')")
    public CommonResult<List<WmsWarehouseZoneSimpleRespVO>> getWarehouseZoneSimpleList(@Valid WmsWarehouseZonePageReqVO pageReqVO) {
        // 查询数据
        List<WmsWarehouseZoneDO> doList = warehouseZoneService.getSimpleList(pageReqVO);
        // 转换
        List<WmsWarehouseZoneSimpleRespVO> voList = BeanUtils.toBean(doList, WmsWarehouseZoneSimpleRespVO.class);

        return success(voList);
    }

    /**
     * @sign : 5C5385F8891360A6
     */
    @GetMapping("/page")
    @Operation(summary = "获得库区分页")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-zone:query')")
    public CommonResult<PageResult<WmsWarehouseZoneRespVO>> getWarehouseZonePage(@Valid WmsWarehouseZonePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsWarehouseZoneDO> doPageResult = warehouseZoneService.getWarehouseZonePage(pageReqVO);
        // 转换
        PageResult<WmsWarehouseZoneRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsWarehouseZoneRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsWarehouseZoneRespVO::getCreator, WmsWarehouseZoneRespVO::setCreatorName)
			.mapping(WmsWarehouseZoneRespVO::getCreator, WmsWarehouseZoneRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库区 Excel")
    @PreAuthorize("@ss.hasPermission('wms:warehouse-zone:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseZoneExcel(@Valid WmsWarehouseZonePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsWarehouseZoneDO> list = warehouseZoneService.getWarehouseZonePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库区.xls", "数据", WmsWarehouseZoneRespVO.class, BeanUtils.toBean(list, WmsWarehouseZoneRespVO.class));
    }
}
