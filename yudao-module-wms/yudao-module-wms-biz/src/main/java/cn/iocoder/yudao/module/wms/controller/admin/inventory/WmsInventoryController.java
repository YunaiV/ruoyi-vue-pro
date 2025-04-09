package cn.iocoder.yudao.module.wms.controller.admin.inventory;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.inventory.bin.WmsInventoryBinService;
import cn.iocoder.yudao.module.wms.service.inventory.product.WmsInventoryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_NOT_EXISTS;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "盘点单")
@RestController
@RequestMapping("/wms/inventory")
@Validated
public class WmsInventoryController {

    @Resource()
    @Lazy()
    private WmsInventoryBinService inventoryBinService;

    @Resource()
    @Lazy()
    private WmsInventoryProductService inventoryProductService;

    @Resource
    private WmsInventoryService inventoryService;

    /**
     * @sign : EEF1FA4365B38CB4
     */
    @PostMapping("/create")
    @Operation(summary = "创建盘点")
    @PreAuthorize("@ss.hasPermission('wms:inventory:create')")
    public CommonResult<Long> createInventory(@Valid @RequestBody WmsInventorySaveReqVO createReqVO) {
        return success(inventoryService.createInventory(createReqVO).getId());
    }

    /**
     * @sign : 30CB28F31026826D
     */
    @PutMapping("/update")
    @Operation(summary = "更新盘点")
    @PreAuthorize("@ss.hasPermission('wms:inventory:update')")
    public CommonResult<Boolean> updateInventory(@Valid @RequestBody WmsInventorySaveReqVO updateReqVO) {
        inventoryService.updateInventory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inventory:delete')")
    public CommonResult<Boolean> deleteInventory(@RequestParam("id") Long id) {
        inventoryService.deleteInventory(id);
        return success(true);
    }

    /**
     * @sign : FD03427E08081E43
     */
    @GetMapping("/get")
    @Operation(summary = "获得盘点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<WmsInventoryRespVO> getInventory(@RequestParam("id") Long id) {
        // 查询数据
        WmsInventoryDO inventory = inventoryService.getInventory(id);
        if (inventory == null) {
            throw exception(INVENTORY_NOT_EXISTS);
        }
        // 转换
        WmsInventoryRespVO inventoryVO = BeanUtils.toBean(inventory, WmsInventoryRespVO.class);
        // 组装库存盘点产品
        List<WmsInventoryProductDO> inventoryProductList = inventoryProductService.selectByInventoryId(inventoryVO.getId());
        inventoryVO.setProductItemList(BeanUtils.toBean(inventoryProductList, WmsInventoryProductRespVO.class));
        // 组装库位盘点
        List<WmsInventoryBinDO> inventoryBinList = inventoryBinService.selectByInventoryId(inventoryVO.getId());
        inventoryVO.setBinItemList(BeanUtils.toBean(inventoryBinList, WmsInventoryBinRespVO.class));
        // 返回
        return success(inventoryVO);
    }

    /**
     * @sign : B1908BBFDE62B4FB
     */
    @GetMapping("/page")
    @Operation(summary = "获得盘点分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<PageResult<WmsInventoryRespVO>> getInventoryPage(@Valid WmsInventoryPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsInventoryDO> doPageResult = inventoryService.getInventoryPage(pageReqVO);
        // 转换
        PageResult<WmsInventoryRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInventoryRespVO.class);
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出盘点 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inventory:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInventoryExcel(@Valid WmsInventoryPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInventoryDO> list = inventoryService.getInventoryPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "盘点.xls", "数据", WmsInventoryRespVO.class, BeanUtils.toBean(list, WmsInventoryRespVO.class));
    }
}