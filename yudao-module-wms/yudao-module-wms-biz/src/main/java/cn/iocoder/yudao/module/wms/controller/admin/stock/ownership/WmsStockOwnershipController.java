package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "所有者库存")
@RestController
@RequestMapping("/wms/stock-ownership")
@Validated
public class WmsStockOwnershipController {

    @Resource
    private WmsStockOwnershipService stockOwnershipService;

    // /**
    // * @sign : 0E0BDA21A9064C48
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建所有者库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-ownership:create')")
    // public CommonResult<Long> createStockOwnership(@Valid @RequestBody WmsStockOwnershipSaveReqVO createReqVO) {
    // return success(stockOwnershipService.createStockOwnership(createReqVO).getId());
    // }
    // /**
    // * @sign : A40903BCE776025E
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新所有者库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-ownership:update')")
    // public CommonResult<Boolean> updateStockOwnership(@Valid @RequestBody WmsStockOwnershipSaveReqVO updateReqVO) {
    // stockOwnershipService.updateStockOwnership(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除所有者库存")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-ownership:delete')")
    // public CommonResult<Boolean> deleteStockOwnership(@RequestParam("id") Long id) {
    // stockOwnershipService.deleteStockOwnership(id);
    // return success(true);
    // }
    /**
     * @sign : 7CDB60ED7A6D3E5E
     */
    @GetMapping("/stocks")
    @Operation(summary = "获得产品的所有者库存")
    @Parameter(name = "warehouseId", description = "仓库ID", required = true, example = "1024")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:query')")
    public CommonResult<List<WmsStockOwnershipRespVO>> selectStockOwnership(@RequestParam("warehouseId") Long warehouseId, @RequestParam("productId") Long productId) {
        // 查询数据
        List<WmsStockOwnershipDO> stockOwnershipList = stockOwnershipService.selectStockOwnership(warehouseId, productId);
        // 转换
        List<WmsStockOwnershipRespVO> stockOwnershipVO = BeanUtils.toBean(stockOwnershipList, WmsStockOwnershipRespVO.class);
        // 返回
        return success(stockOwnershipVO);
    }

    /**
     * @sign : EC951F0579860D97
     */
    @GetMapping("/page")
    @Operation(summary = "获得所有者库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership:query')")
    public CommonResult<PageResult<WmsStockOwnershipRespVO>> getStockOwnershipPage(@Valid WmsStockOwnershipPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockOwnershipDO> doPageResult = stockOwnershipService.getStockOwnershipPage(pageReqVO);
        // 转换
        PageResult<WmsStockOwnershipRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockOwnershipRespVO.class);
        // 装配模型
        stockOwnershipService.assembleProducts(voPageResult.getList());
        stockOwnershipService.assembleWarehouse(voPageResult.getList());
        stockOwnershipService.assembleDept(voPageResult.getList());
        stockOwnershipService.assembleCompany(voPageResult.getList());

        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出所有者库存 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-ownership:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockOwnershipExcel(@Valid WmsStockOwnershipPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockOwnershipDO> list = stockOwnershipService.getStockOwnershipPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "所有者库存.xls", "数据", WmsStockOwnershipRespVO.class, BeanUtils.toBean(list, WmsStockOwnershipRespVO.class));
    // }
}
