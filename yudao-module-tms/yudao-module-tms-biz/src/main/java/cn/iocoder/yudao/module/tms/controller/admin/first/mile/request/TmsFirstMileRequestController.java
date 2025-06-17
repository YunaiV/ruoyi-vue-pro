package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.system.enums.common.CountryEnum;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsProductRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo.TmsFirstMileRequestItemRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.*;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.request.TmsFirstMileRequestProductStockRespVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileRequestBO;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import cn.iocoder.yudao.module.wms.api.stock.logic.WmsStockLogicApi;
import cn.iocoder.yudao.module.wms.api.stock.logic.dto.WmsStockLogicDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - TMS头程申请单")
@RestController
@RequestMapping("/tms/first-mile-request")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TmsFirstMileRequestController {

    private final TmsFirstMileRequestService firstMileRequestService;
    private final ErpProductApi erpProductApi;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final DeptApi deptApi;
    private final AdminUserApi adminUserApi;
    private final FmsCompanyApi fmsCompanyApi;
    private final WmsStockLogicApi wmsStockLogicApi;

    @PostMapping("/create")
    @Operation(summary = "创建头程申请单")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:create')")
    public CommonResult<Long> createFirstMileRequest(@Validated(Validation.OnCreate.class) @RequestBody TmsFirstMileRequestSaveReqVO vo) {
        return success(firstMileRequestService.createFirstMileRequest(vo));
    }

    @PutMapping("/update")
    @Operation(summary = "更新头程申请单")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:update')")
    public CommonResult<Boolean> updateFirstMileRequest(@Validated(Validation.OnUpdate.class) @RequestBody TmsFirstMileRequestSaveReqVO vo) {

        firstMileRequestService.updateFirstMileRequest(vo);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除头程申请单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:delete')")
    public CommonResult<Boolean> deleteFirstMileRequest(@RequestParam("id") Long id) {
        firstMileRequestService.deleteFirstMileRequest(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得头程申请单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:query')")
    public CommonResult<TmsFirstMileRequestRespVO> getFirstMileRequest(@RequestParam("id") Long id) {
        TmsFirstMileRequestBO firstMileRequestBO = firstMileRequestService.getFirstMileRequestBO(id);
        if (firstMileRequestBO == null) {
            return success(null);
        }
        // 转换为响应对象
        List<TmsFirstMileRequestRespVO> respVOList = bindListResult(Collections.singletonList(firstMileRequestBO));
        return success(respVOList.get(0));
    }

    @PostMapping("/page")
    @Operation(summary = "获得头程申请单分页")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:query')")
    public CommonResult<PageResult<TmsFirstMileRequestRespVO>> getFirstMileRequestPage(@Validated @RequestBody(required = false) TmsFirstMileRequestPageReqVO pageReqVO) {
        PageResult<TmsFirstMileRequestBO> pageBO = firstMileRequestService.getFirstMileRequestBOPage(pageReqVO);
        List<TmsFirstMileRequestRespVO> respVOList = bindListResult(pageBO.getList());
        // 创建结果对象
        PageResult<TmsFirstMileRequestRespVO> pageResultRespVO = new PageResult<>();
        pageResultRespVO.setTotal(pageBO.getTotal());
        pageResultRespVO.setList(respVOList);
        return success(pageResultRespVO);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出头程申请单 Excel")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFirstMileRequestExcel(@Validated TmsFirstMileRequestPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        // 获取分页数据
        PageResult<TmsFirstMileRequestBO> pageBO = firstMileRequestService.getFirstMileRequestBOPage(pageReqVO);
        // 转换为响应对象列表
        List<TmsFirstMileRequestRespVO> list = bindListResult(pageBO.getList());
        // 导出 Excel文件
        ExcelUtils.write(response, "头程申请单.xls", "数据", TmsFirstMileRequestRespVO.class, list);
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入头程申请单 Excel")
    @ApiAccessLog(operateType = IMPORT)
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:import')")
    public CommonResult<Boolean> importFirstMileRequestExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TmsFirstMileRequestSaveReqVO> list = ExcelUtils.read(file, TmsFirstMileRequestSaveReqVO.class);
        // 可根据业务需要批量保存或校验
        return success(true);
    }

    @PutMapping("/submit-audit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:submit-audit')")
    public CommonResult<Boolean> submitAudit(@Validated @RequestBody TmsFirstMileRequestSubmitAuditReqVO reqVO) {
        firstMileRequestService.submitAudit(reqVO.getIds());
        return success(true);
    }

    @PutMapping("/audit-status")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:audit-status')")
    public CommonResult<Boolean> audit(@RequestBody TmsFirstMileRequestAuditReqVO reqVO) {
        firstMileRequestService.review(reqVO);
        return success(true);
    }

    //启用/关闭申请单子项
    @PutMapping("/update-item-status")
    @Operation(summary = "启用/禁用申请单子项")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:item-off')")
    public CommonResult<Boolean> updateItemStatus(@Validated @RequestBody TmsFirstMileRequestItemOffReqVO reqVO) {
        firstMileRequestService.switchTmsFirstMileOpenStatus(reqVO.getItemIds(), reqVO.getEnable());
        return success(true);
    }

    //获取最新的单据编号
    @GetMapping("/get-latest-no")
    @Operation(summary = "获取最新的单据编号")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:get-latest-code')")
    public CommonResult<String> getLatestNo() {
        return success(firstMileRequestService.getLatestCode());
    }

    @PostMapping("/merge")
    @Operation(summary = "合并头程申请单")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:merge')")
    public CommonResult<Long> mergeFirstMileRequest(@Validated(Validation.OnCreate.class) @RequestBody TmsFirstMileSaveReqVO createReqVO) {
        return success(firstMileRequestService.mergeFirstMileRequest(createReqVO));
    }

    @PostMapping("/get-product-stock")
    @Operation(summary = "获取产品可用库存")
    @PreAuthorize("@ss.hasPermission('tms:first-mile-request:query')")
    public CommonResult<TmsFirstMileRequestProductStockRespVO> getProductStock(@Validated @RequestBody TmsFirstMileRequestProductStockReqVO reqVO) {
        Map<Long, WmsStockLogicDTO> stockMap = wmsStockLogicApi.selectByDeptIdAndProductIdAndCountryIdMap(reqVO.getDeptId(), reqVO.getProductIds(), reqVO.getCountry());
        
        // 转换为 ProductStock 列表
        List<TmsFirstMileRequestProductStockRespVO.ProductStock> productStocks = stockMap.entrySet().stream()
                .map(entry -> {
                    TmsFirstMileRequestProductStockRespVO.ProductStock stock = new TmsFirstMileRequestProductStockRespVO.ProductStock();
                    stock.setProductId(entry.getKey());
                    stock.setAvailableQty(entry.getValue().getAvailableQty());
                    return stock;
                })
                .collect(Collectors.toList());

        // 构建返回对象
        TmsFirstMileRequestProductStockRespVO respVO = new TmsFirstMileRequestProductStockRespVO();
        respVO.setProductStocks(productStocks);
        return success(respVO);
    }

    private List<TmsFirstMileRequestRespVO> bindListResult(List<TmsFirstMileRequestBO> firstMileRequestBOList) {
        if (firstMileRequestBOList == null || firstMileRequestBOList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> productIds = firstMileRequestBOList.stream().flatMap(bo -> bo.getItems().stream()).map(TmsFirstMileRequestItemDO::getProductId).distinct()
            .collect(Collectors.toList());
        List<Long> warehouseIds = firstMileRequestBOList.stream().map(TmsFirstMileRequestBO::getToWarehouseId).distinct().collect(Collectors.toList());
        //创建更新人，主子表
        // 收集所有创建人和更新人ID
        Set<Long> userIds = firstMileRequestBOList.stream()
            .flatMap(bo -> Stream.concat(
                Stream.of(bo.getCreator(), bo.getUpdater(), bo.getRequesterId() == null ? null : bo.getRequesterId().toString()),
                bo.getItems() == null ? Stream.empty() :
                    bo.getItems().stream().flatMap(item -> Stream.of(item.getCreator(), item.getUpdater()))
            ))
            .filter(Objects::nonNull)
            .map(this::safeParseLong)
            .collect(Collectors.toSet());
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(firstMileRequestBOList.stream().map(TmsFirstMileRequestBO::getRequestDeptId).distinct().collect(Collectors.toList()));
        // 获取用户Map
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(productIds);
        Map<Long, WmsWarehouseDTO> warehouseMap = wmsWarehouseApi.getWarehouseMap(warehouseIds);
        Map<Long, FmsCompanyDTO> dtoMap = fmsCompanyApi.getCompanyMap(firstMileRequestBOList.stream()
                .flatMap(bo -> bo.getItems().stream().map(TmsFirstMileRequestItemDO::getSalesCompanyId)).collect(Collectors.toSet()));
        //获取产品库存 wmsWarehouseApi
        //公司MAP
        Map<Long, WmsStockLogicDTO> wmsStockLogicDTOMap = wmsStockLogicApi.selectByDeptIdAndProductIdAndCountryIdMap(firstMileRequestBOList.get(0).getRequestDeptId(), productIds, CountryEnum.CHINA.getCountryCode());

        return firstMileRequestBOList.stream().map(bo -> {
            TmsFirstMileRequestRespVO respVO = BeanUtils.toBean(bo, TmsFirstMileRequestRespVO.class, respVO1 -> {
                MapUtils.findAndThen(warehouseMap, bo.getToWarehouseId(), warehouse -> respVO1.setToWarehouseName(warehouse.getName()));
                MapUtils.findAndThen(deptMap, bo.getRequestDeptId(), dept -> respVO1.setRequestDeptName(dept.getName()));
                MapUtils.findAndThen(userMap, safeParseLong(bo.getCreator()), user -> respVO1.setCreator(user.getNickname()));
                MapUtils.findAndThen(userMap, safeParseLong(bo.getUpdater()), user -> respVO1.setUpdater(user.getNickname()));
                MapUtils.findAndThen(userMap, bo.getRequesterId(), user -> respVO1.setRequestUserName(user.getNickname()));
            });
            //设置出运跟踪信息

            if (bo.getItems() != null) {
                List<TmsFirstMileRequestItemRespVO> items = bo.getItems().stream().map(item ->
                    BeanUtils.toBean(item, TmsFirstMileRequestItemRespVO.class, itemRespVO -> {
                        MapUtils.findAndThen(productMap, item.getProductId(), product -> itemRespVO.setProduct(BeanUtils.toBean(product, TmsProductRespVO.class)));
                        MapUtils.findAndThen(dtoMap, item.getSalesCompanyId(), company -> itemRespVO.setSalesCompanyName(company.getName()));
                        MapUtils.findAndThen(userMap, safeParseLong(item.getCreator()), user -> itemRespVO.setCreator(user.getNickname()));
                        MapUtils.findAndThen(userMap, safeParseLong(item.getUpdater()), user -> itemRespVO.setUpdater(user.getNickname()));
                        //带出该申请部门的 该产品sku的 中国的 仓库库存汇总
                        MapUtils.findAndThen(wmsStockLogicDTOMap, item.getProductId(), wmsStockLogicDTO -> itemRespVO.setDomesticWarehouseStock(wmsStockLogicDTO.getAvailableQty()));

                        // 计算总长宽高和重量
                        if (item.getQty() != null && item.getQty() > 0) {
                            // 计算总长宽高
                            if (item.getPackageLength() != null) {
                                itemRespVO.setTotalPackageLength(item.getPackageLength().multiply(new BigDecimal(item.getQty())));
                            }
                            if (item.getPackageWidth() != null) {
                                itemRespVO.setTotalPackageWidth(item.getPackageWidth().multiply(new BigDecimal(item.getQty())));
                            }
                            if (item.getPackageHeight() != null) {
                                itemRespVO.setTotalPackageHeight(item.getPackageHeight().multiply(new BigDecimal(item.getQty())));
                            }

                            // 计算总重量
                            if (item.getWeight() != null) {
                                itemRespVO.setTotalWeight(item.getWeight().multiply(new BigDecimal(item.getQty())));
                            }

                            // 计算总包装重量
                            if (item.getPackageWeight() != null) {
                                itemRespVO.setTotalPackageWeight(item.getPackageWeight().multiply(new BigDecimal(item.getQty())));
                            }

                            // 计算总体积（立方毫米）
                            if (item.getPackageLength() != null && item.getPackageWidth() != null && item.getPackageHeight() != null) {
                                // 计算单个产品体积（立方毫米）
                                BigDecimal singleVolume = item.getPackageLength()
                                    .multiply(item.getPackageWidth())
                                    .multiply(item.getPackageHeight());
                                itemRespVO.setVolume(singleVolume);

                                // 计算总体积（立方毫米）
                                BigDecimal volume = singleVolume.multiply(new BigDecimal(item.getQty()));
                                itemRespVO.setTotalVolume(volume);
                            }
                        }
                    })).collect(Collectors.toList());

                respVO.setItems(items);
                // 设置明细数量
                respVO.setItemCount(items.size());

                // 计算总重量和总体积
                BigDecimal totalWeight = BigDecimal.ZERO;
                BigDecimal totalPackageWeight = BigDecimal.ZERO;
                BigDecimal totalVolume = BigDecimal.ZERO;
                for (TmsFirstMileRequestItemRespVO item : items) {
                    if (item.getTotalWeight() != null) {
                        totalWeight = totalWeight.add(item.getTotalWeight());
                    }
                    if (item.getTotalPackageWeight() != null) {
                        totalPackageWeight = totalPackageWeight.add(item.getTotalPackageWeight());
                    }
                    if (item.getTotalVolume() != null) {
                        totalVolume = totalVolume.add(item.getTotalVolume());
                    }
                }
                respVO.setTotalWeight(totalWeight);
                respVO.setTotalPackageWeight(totalPackageWeight);
                respVO.setTotalVolume(totalVolume);
            } else {
                respVO.setItemCount(0);
                respVO.setTotalWeight(BigDecimal.ZERO);
                respVO.setTotalPackageWeight(BigDecimal.ZERO);
                respVO.setTotalVolume(BigDecimal.ZERO);
            }
            return respVO;
        }).collect(Collectors.toList());
    }

    private Long safeParseLong(String value) {
        try {
            return Optional.ofNullable(value).map(Long::parseLong).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}