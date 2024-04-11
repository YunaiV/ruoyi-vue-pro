package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.returns.ErpSaleReturnPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.returns.ErpSaleReturnRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.returns.ErpSaleReturnSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleReturnService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 销售退货")
@RestController
@RequestMapping("/erp/sale-return")
@Validated
public class ErpSaleReturnController {

    @Resource
    private ErpSaleReturnService saleReturnService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpCustomerService customerService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建销售退货")
    @PreAuthorize("@ss.hasPermission('erp:sale-return:create')")
    public CommonResult<Long> createSaleReturn(@Valid @RequestBody ErpSaleReturnSaveReqVO createReqVO) {
        return success(saleReturnService.createSaleReturn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售退货")
    @PreAuthorize("@ss.hasPermission('erp:sale-return:update')")
    public CommonResult<Boolean> updateSaleReturn(@Valid @RequestBody ErpSaleReturnSaveReqVO updateReqVO) {
        saleReturnService.updateSaleReturn(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新销售退货的状态")
    @PreAuthorize("@ss.hasPermission('erp:sale-return:update-status')")
    public CommonResult<Boolean> updateSaleReturnStatus(@RequestParam("id") Long id,
                                                      @RequestParam("status") Integer status) {
        saleReturnService.updateSaleReturnStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售退货")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sale-return:delete')")
    public CommonResult<Boolean> deleteSaleReturn(@RequestParam("ids") List<Long> ids) {
        saleReturnService.deleteSaleReturn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售退货")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sale-return:query')")
    public CommonResult<ErpSaleReturnRespVO> getSaleReturn(@RequestParam("id") Long id) {
        ErpSaleReturnDO saleReturn = saleReturnService.getSaleReturn(id);
        if (saleReturn == null) {
            return success(null);
        }
        List<ErpSaleReturnItemDO> saleReturnItemList = saleReturnService.getSaleReturnItemListByReturnId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(saleReturnItemList, ErpSaleReturnItemDO::getProductId));
        return success(BeanUtils.toBean(saleReturn, ErpSaleReturnRespVO.class, saleReturnVO ->
                saleReturnVO.setItems(BeanUtils.toBean(saleReturnItemList, ErpSaleReturnRespVO.Item.class, item -> {
                    ErpStockDO stock = stockService.getStock(item.getProductId(), item.getWarehouseId());
                    item.setStockCount(stock != null ? stock.getCount() : BigDecimal.ZERO);
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售退货分页")
    @PreAuthorize("@ss.hasPermission('erp:sale-return:query')")
    public CommonResult<PageResult<ErpSaleReturnRespVO>> getSaleReturnPage(@Valid ErpSaleReturnPageReqVO pageReqVO) {
        PageResult<ErpSaleReturnDO> pageResult = saleReturnService.getSaleReturnPage(pageReqVO);
        return success(buildSaleReturnVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售退货 Excel")
    @PreAuthorize("@ss.hasPermission('erp:sale-return:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSaleReturnExcel(@Valid ErpSaleReturnPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpSaleReturnRespVO> list = buildSaleReturnVOPageResult(saleReturnService.getSaleReturnPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "销售退货.xls", "数据", ErpSaleReturnRespVO.class, list);
    }

    private PageResult<ErpSaleReturnRespVO> buildSaleReturnVOPageResult(PageResult<ErpSaleReturnDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 退货项
        List<ErpSaleReturnItemDO> saleReturnItemList = saleReturnService.getSaleReturnItemListByReturnIds(
                convertSet(pageResult.getList(), ErpSaleReturnDO::getId));
        Map<Long, List<ErpSaleReturnItemDO>> saleReturnItemMap = convertMultiMap(saleReturnItemList, ErpSaleReturnItemDO::getReturnId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(saleReturnItemList, ErpSaleReturnItemDO::getProductId));
        // 1.3 客户信息
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(pageResult.getList(), ErpSaleReturnDO::getCustomerId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), saleReturn -> Long.parseLong(saleReturn.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpSaleReturnRespVO.class, saleReturn -> {
            saleReturn.setItems(BeanUtils.toBean(saleReturnItemMap.get(saleReturn.getId()), ErpSaleReturnRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()))));
            saleReturn.setProductNames(CollUtil.join(saleReturn.getItems(), "，", ErpSaleReturnRespVO.Item::getProductName));
            MapUtils.findAndThen(customerMap, saleReturn.getCustomerId(), supplier -> saleReturn.setCustomerName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(saleReturn.getCreator()), user -> saleReturn.setCreatorName(user.getNickname()));
        });
    }

}