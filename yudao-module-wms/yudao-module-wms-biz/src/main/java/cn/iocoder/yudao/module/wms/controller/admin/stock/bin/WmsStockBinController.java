package cn.iocoder.yudao.module.wms.controller.admin.stock.bin;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespBinVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinExcelVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicPureRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
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
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "仓位库存")
@RestController
@RequestMapping("/wms/stock-bin")
@Validated
public class WmsStockBinController {

    @Resource
    private WmsStockBinService stockBinService;

    @Resource
    private WmsInboundService inboundService;

    // /**
    // * @sign : 7472CDCA246B810A
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建仓位库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin:create')")
    // public CommonResult<Long> createStockBin(@Valid @RequestBody WmsStockBinSaveReqVO createReqVO) {
    // return success(stockBinService.createStockBin(createReqVO).getId());
    // }
    // /**
    // * @sign : AC84893CE186DA40
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新仓位库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin:update')")
    // public CommonResult<Boolean> updateStockBin(@Valid @RequestBody WmsStockBinSaveReqVO updateReqVO) {
    // stockBinService.updateStockBin(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除仓位库存")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin:delete')")
    // public CommonResult<Boolean> deleteStockBin(@RequestParam("id") Long id) {
    // stockBinService.deleteStockBin(id);
    // return success(true);
    // }
    /**
     * @sign : D7B68E7D4D845527
     */
    @GetMapping("/stocks")
    @Operation(summary = "获得产品的仓位库存")
    @Parameter(name = "warehouseId", description = "仓库ID", required = true, example = "1024")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<List<WmsStockBinRespVO>> getStockBin(@RequestParam("warehouseId") Long warehouseId,@RequestParam("binId") Long binId, @RequestParam("productId") Long productId) {
        // 查询数据
        List<WmsStockBinDO> stockBinList = stockBinService.selectStockBin(warehouseId,binId, productId);
        // 转换
        List<WmsStockBinRespVO> stockBinVO = BeanUtils.toBean(stockBinList, WmsStockBinRespVO.class);
        // 返回
        return success(stockBinVO);
    }

    /**
     * @sign : 33164D4484F99F37
     */
    @PostMapping("/page")
    @Operation(summary = "获得仓位库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<PageResult<WmsStockBinRespVO>> getStockBinPage(@Valid @RequestBody WmsStockBinPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockBinDO> doPageResult = stockBinService.getStockBinPage(pageReqVO);


        // 转换
        PageResult<WmsStockBinRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockBinRespVO.class);
        // 装配
        stockBinService.assembleProducts(voPageResult.getList());
        stockBinService.assembleWarehouse(voPageResult.getList());
        stockBinService.assembleBin(voPageResult.getList(),true);

        // 获取建议库存
        if (Objects.equals(1, pageReqVO.getWithSuggestedLogic())) {
            //List<WmsStockLogicPureRespVO> suggestedLogicList = new ArrayList<>();
            Map<Long, List<Long>> productIdsMap = StreamX.from(voPageResult.getList()).groupBy(WmsStockBinRespVO::getWarehouseId,WmsStockBinRespVO::getProductId);

            for (Map.Entry<Long, List<Long>> entry : productIdsMap.entrySet()) {
                Map<Long, WmsInboundItemLogicDO> logicMap = inboundService.getInboundItemLogicMap(entry.getKey(), entry.getValue(), true);

                StreamX.from(voPageResult.getList())
                    .filter(e-> Objects.equals(entry.getKey(),e.getWarehouseId()))
                    .forEach(e->{
                        WmsInboundItemLogicDO logic = logicMap.get(e.getProductId());
                        e.setSuggestedLogic(BeanUtils.toBean(logic, WmsStockLogicPureRespVO.class));
                    });
            }

        }

        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
            .mapping(WmsStockBinRespVO::getCreator, WmsStockBinRespVO::setCreatorName)
            .mapping(WmsStockBinRespVO::getUpdater, WmsStockBinRespVO::setUpdaterName)
            .fill();
        //过滤 3个都是0的,可用量,待出库量,可售量，
        // TODO待优化，mapper限定
        voPageResult.getList().removeIf(e -> Objects.equals(e.getAvailableQty(), 0) && Objects.equals(e.getOutboundPendingQty(), 0) && Objects.equals(e.getSellableQty(), 0));
        // 返回
        return success(voPageResult);
    }

    @PostMapping("/grouped-page")
    @Operation(summary = "获得按产品分组的仓位库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<PageResult<WmsProductRespBinVO>> getGroupedStockBinPage(@Valid @RequestBody WmsStockBinPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsProductRespBinVO> voPageResult = stockBinService.getGroupedStockBinPage(pageReqVO);

        stockBinService.assembleDept(voPageResult.getList());
        stockBinService.assembleStockWarehouseList(pageReqVO.getWarehouseId(),voPageResult.getList());

        // 返回
        return success(voPageResult);
    }


    @PostMapping("/export-excel")
    @Operation(summary = "导出仓位库存 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockBinExcel(@Valid @RequestBody WmsStockBinPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockBinRespVO> list = this.getStockBinPage(pageReqVO).getData().getList();
        List<WmsStockBinExcelVO> xlsList = BeanUtils.toBean(list, WmsStockBinExcelVO.class);
        Map<Long, WmsStockBinRespVO> voMap= StreamX.from(list).toMap(WmsStockBinRespVO::getId);
        for (WmsStockBinExcelVO xlsVO : xlsList) {
            WmsStockBinRespVO vo=voMap.get(xlsVO.getId());
            if(vo==null) {
                continue;
            }
            xlsVO.setWarehouseName(vo.getWarehouse().getName());
            xlsVO.setBinName(vo.getBin().getName());
            xlsVO.setProductCode(vo.getProduct().getCode());
        }

        // 导出 Excel
        ExcelUtils.write(response, "仓位库存.xls", "数据", WmsStockBinExcelVO.class,xlsList);
    }
}

