package cn.iocoder.yudao.module.tms.controller.admin.first.mile;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileAuditReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMilePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileStockQueryReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileExcelVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileStockListRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileStockRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingRespVO;
import cn.iocoder.yudao.module.tms.convert.first.mile.TmsFirstMileConvert;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileBO;
import cn.iocoder.yudao.module.tms.service.first.mile.TmsFirstMileService;
import cn.iocoder.yudao.module.wms.api.inbound.item.WmsInboundItemApi;
import cn.iocoder.yudao.module.wms.api.inbound.item.dto.WmsInboundItemBinDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - TMS头程单")
@RestController
@RequestMapping("/tms/first-mile")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TmsFirstMileController {

    private final TmsFirstMileService firstMileService;
    private final AdminUserApi adminUserApi;
    private final FmsCompanyApi fmsCompanyApi;
    private final ErpProductApi erpProductApi;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final DeptApi deptApi;
    private final WmsInboundItemApi wmsInboundItemApi;
    private final TmsFirstMileService tmsFirstMileService;

    @PostMapping("/create")
    @Operation(summary = "创建头程单")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:create')")
    public CommonResult<Long> createFirstMile(@RequestBody TmsFirstMileSaveReqVO vo) {
        return success(firstMileService.createFirstMile(vo));
    }

    @PutMapping("/update")
    @Operation(summary = "更新头程单")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:update')")
    public CommonResult<Boolean> updateFirstMile(@Validated(Validation.OnUpdate.class) @RequestBody TmsFirstMileSaveReqVO vo) {
        firstMileService.updateFirstMile(vo);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除头程单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:first-mile:delete')")
    public CommonResult<Boolean> deleteFirstMile(@RequestParam("id") Long id) {
        firstMileService.deleteFirstMile(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得头程单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:first-mile:query')")
    public CommonResult<TmsFirstMileRespVO> getFirstMile(@RequestParam("id") Long id) {
        TmsFirstMileBO firstMile = firstMileService.getFirstMileBO(id);
        List<TmsFirstMileRespVO> mileRespVOS = bindResult(Collections.singletonList(firstMile));
        if (mileRespVOS == null || mileRespVOS.isEmpty()) {
            return success(null);
        }
        firstMileService.assembleTmsFirstMileStockRespVO(mileRespVOS.get(0));
        return success(mileRespVOS.get(0));
    }

    @PostMapping("/page")
    @Operation(summary = "获得头程单分页")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:query')")
    public CommonResult<PageResult<TmsFirstMileRespVO>> getFirstMilePage(@Valid @RequestBody(required = false) TmsFirstMilePageReqVO pageReqVO) {
        PageResult<TmsFirstMileBO> pageResult = firstMileService.getFirstMileBOPage(pageReqVO);
        return success(new PageResult<>(bindResult(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出头程单 Excel")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExcel(@Valid TmsFirstMilePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TmsFirstMileBO> list = firstMileService.getFirstMileBOPage(pageReqVO).getList();
        // 导出 Excel
        List<TmsFirstMileExcelVO> excelList = TmsFirstMileConvert.convertExcelList(list);
        ExcelUtils.write(response, "头程单.xlsx", "数据", TmsFirstMileExcelVO.class, excelList);
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入头程单 Excel")
    @ApiAccessLog(operateType = IMPORT)
    @PreAuthorize("@ss.hasPermission('tms:first-mile:import')")
    public CommonResult<Boolean> importFirstMileExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TmsFirstMileSaveReqVO> list = ExcelUtils.read(file, TmsFirstMileSaveReqVO.class);
        // 可根据业务需要批量保存或校验
        return success(true);
    }

    // ==================== 子表（出运订单费用明细） ====================

    @GetMapping("/fee/list-by-source-id")
    @Operation(summary = "获得出运订单费用明细列表")
    @Parameter(name = "sourceId", description = "头程单ID")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:query')")
    public CommonResult<List<TmsFeeRespVO>> getFeeListBySourceId(@RequestParam("sourceId") Long sourceId) {
        return success(firstMileService.getFeeListBySourceId(sourceId));
    }

    @PutMapping("/submit-audit")
    @Operation(summary = "提交头程单审核")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:audit')")
    public CommonResult<Boolean> submitAudit(@RequestBody @Size(min = 1, message = "提交审核的单据数量不小于1") List<Long> ids) {
        firstMileService.submitAudit(ids);
        return success(true);
    }

    @PutMapping("/audit-status")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:review')")
    public CommonResult<Boolean> audit(@Validated @RequestBody TmsFirstMileAuditReqVO reqVO) {
        firstMileService.review(reqVO);
        return success(true);
    }

    @GetMapping("/get-latest-no")
    @Operation(summary = "获取最新的单据编号")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:query')")
    public CommonResult<String> getLatestNo() {
        return success(firstMileService.getLatestCode());
    }

    @PostMapping("/stock/list")
    @Operation(summary = "批量查询产品库存信息")
    @PreAuthorize("@ss.hasPermission('tms:first-mile:query')")
    public CommonResult<TmsFirstMileStockListRespVO> getStockList(@Validated @RequestBody TmsFirstMileStockQueryReqVO reqVO) {
        // 1. 获取所有产品ID
        Set<Long> productIds = reqVO.getRelations().stream()
                .map(TmsFirstMileStockQueryReqVO.ProductDeptRelation::getProductId)
                .collect(Collectors.toSet());

        // 2. 调用WMS API获取库存信息
        Map<Long, List<WmsInboundItemBinDTO>> stockMap = wmsInboundItemApi.getInboundItemBinMap(
                reqVO.getWarehouseId(), productIds, true);

        // 3. 转换为前端VO
        List<TmsFirstMileStockListRespVO.ProductStockVO> productStocks = new ArrayList<>();
        stockMap.forEach((productId, stockList) -> {
            // 获取该产品对应的部门ID
            Set<Long> deptIds = reqVO.getRelations().stream()
                    .filter(relation -> relation.getProductId().equals(productId))
                    .map(TmsFirstMileStockQueryReqVO.ProductDeptRelation::getDeptId)
                    .collect(Collectors.toSet());

            List<TmsFirstMileStockRespVO> voList = stockList.stream()
                    .filter(stock -> deptIds.contains(stock.getInboundDeptId())) // 过滤部门
                    .map(stock -> BeanUtils.toBean(stock, TmsFirstMileStockRespVO.class))
                    .collect(Collectors.toList());

            // 创建产品库存VO
            TmsFirstMileStockListRespVO.ProductStockVO productStockVO = new TmsFirstMileStockListRespVO.ProductStockVO();
            productStockVO.setProductId(productId);
            productStockVO.setStocks(voList);
            productStocks.add(productStockVO);
        });

        // 4. 封装返回结果
        TmsFirstMileStockListRespVO respVO = new TmsFirstMileStockListRespVO();
        respVO.setProductStocks(productStocks);
        return success(respVO);
    }

    private List<TmsFirstMileRespVO> bindResult(List<TmsFirstMileBO> beans) {
        if (CollUtil.isEmpty(beans)) {
            return Collections.emptyList();
        }
        // 收集所有创建人和更新人ID
        Set<Long> userIds = beans.stream()
            .flatMap(bo -> Stream.concat(
                Stream.of(bo.getCreator(), bo.getUpdater(), bo.getAuditorId() == null ? null : bo.getAuditorId().toString()),
                Stream.concat(
                    bo.getItems() == null ? Stream.empty() : bo.getItems().stream().flatMap(item -> Stream.of(item.getCreator(), item.getUpdater())),
                    bo.getFees() == null ? Stream.empty() : bo.getFees().stream().flatMap(fee -> Stream.of(fee.getCreator(), fee.getUpdater()))
                )
            ))
            .filter(Objects::nonNull)
            .map(this::safeParseLong)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        //用户
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        //公司
        Set<Long> companyIds = beans.stream().flatMap(bo -> Stream.concat(
                Stream.of(
                    bo.getExportCompanyId(),
                    bo.getTransitCompanyId(),
                    bo.getTracking() != null ? bo.getTracking().getCarrierCompanyId() : null,
                    bo.getTracking() != null ? bo.getTracking().getForwarderCompanyId() : null
                ),
                Stream.concat(
                bo.getItems() == null ? Stream.empty() : bo.getItems().stream().map(TmsFirstMileItemDO::getCompanyId),
                    bo.getItems() == null ? Stream.empty() : bo.getItems().stream().map(TmsFirstMileItemDO::getSalesCompanyId)
                )
            ))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, FmsCompanyDTO> companyMap = fmsCompanyApi.getCompanyMap(companyIds);
        //产品
        Set<Long> productIds = beans.stream().flatMap(bo -> bo.getItems() == null ? Stream.empty() : bo.getItems().stream().map(TmsFirstMileItemDO::getProductId))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(productIds);
        //库存
        //部门
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(beans.stream()
            .flatMap(bo -> bo.getItems() == null ? Stream.empty() : bo.getItems().stream().map(TmsFirstMileItemDO::getDeptId)).collect(Collectors.toSet()));
        //仓库
        Map<Long, WmsWarehouseDTO> warehouseMap = wmsWarehouseApi.getWarehouseMap(beans.stream()
            .flatMap(bo -> Stream.concat(
                bo.getItems() == null ? Stream.empty() : bo.getItems().stream().map(TmsFirstMileItemDO::getFromWarehouseId),
                bo.getToWarehouseId() == null ? Stream.empty() : Stream.of(bo.getToWarehouseId())
            ))
            .collect(Collectors.toSet()));
        //申请单MAP，根据requestItemId
        Map<Long, TmsFirstMileRequestDO> requestMap = tmsFirstMileService.getRequestMap(beans.stream()
                .flatMap(bo -> bo.getItems() == null ? Stream.empty() : bo.getItems().stream().map(TmsFirstMileItemDO::getRequestItemId))
                .collect(Collectors.toSet()));

        return beans.stream().map(bo -> {
            TmsFirstMileRespVO respVO = BeanUtils.toBean(bo, TmsFirstMileRespVO.class);
            //创建人和更新人
            MapUtils.findAndThen(userMap, safeParseLong(bo.getCreator()), user -> respVO.setCreator(user.getNickname()));
            MapUtils.findAndThen(userMap, safeParseLong(bo.getUpdater()), user -> respVO.setUpdater(user.getNickname()));
            MapUtils.findAndThen(userMap, bo.getAuditorId(), user -> respVO.setAuditorName(user.getNickname()));
            //公司
            MapUtils.findAndThen(companyMap, bo.getExportCompanyId(), company -> respVO.setExportCompanyShortName(company.getAbbr()));
            MapUtils.findAndThen(companyMap, bo.getTransitCompanyId(), company -> respVO.setTransitCompanyShortName(company.getAbbr()));
            //仓库
            MapUtils.findAndThen(warehouseMap, bo.getToWarehouseId(), warehouse -> respVO.setToWarehouseName(warehouse.getName()));

            // 设置明细项
            if (CollUtil.isNotEmpty(bo.getItems())) {
                List<TmsFirstMileItemRespVO> items = bo.getItems().stream().map(item -> {
                    TmsFirstMileItemRespVO itemRespVO = BeanUtils.toBean(item, TmsFirstMileItemRespVO.class);
                    //创建人和更新人
                    MapUtils.findAndThen(userMap, safeParseLong(item.getCreator()), user -> itemRespVO.setCreator(user.getNickname()));
                    MapUtils.findAndThen(userMap, safeParseLong(item.getUpdater()), user -> itemRespVO.setUpdater(user.getNickname()));
                    //销售公司
                    MapUtils.findAndThen(companyMap, item.getCompanyId(), company -> itemRespVO.setCompanyName(company.getAbbr()));
                    MapUtils.findAndThen(companyMap, item.getSalesCompanyId(), company -> itemRespVO.setSalesCompanyName(company.getAbbr()));
                    //产品
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> itemRespVO.setProductName(product.getName()).setProductSku(product.getCode()));
                    //部门
                    MapUtils.findAndThen(deptMap, item.getDeptId(), dept -> itemRespVO.setDeptName(dept.getName()));
                    //仓库
                    MapUtils.findAndThen(warehouseMap, item.getFromWarehouseId(), warehouse -> itemRespVO.setFromWarehouseName(warehouse.getName()));
                    //上游单据CODE
                    MapUtils.findAndThen(requestMap, item.getRequestItemId(), request -> itemRespVO.setRequestCode(request.getCode()));

                    return itemRespVO;
                }).collect(Collectors.toList());
                respVO.setFirstMileItems(items);
                // 设置明细汇总box
                respVO.setTotalBoxQty(items.stream().mapToInt(TmsFirstMileItemRespVO::getBoxQty).sum());
            }
            // 设置费用信息 1:N
            if (CollUtil.isNotEmpty(bo.getFees())) {
                List<TmsFeeRespVO> tmsFeeRespVOS = BeanUtils.toBean(bo.getFees(), TmsFeeRespVO.class);
                tmsFeeRespVOS.forEach(tmsFeeRespVO -> {
                    MapUtils.findAndThen(userMap, safeParseLong(tmsFeeRespVO.getUpdater()), user -> tmsFeeRespVO.setUpdater(user.getNickname()));
                    MapUtils.findAndThen(userMap, safeParseLong(tmsFeeRespVO.getCreator()), user -> tmsFeeRespVO.setCreator(user.getNickname()));
                });
                respVO.setFees(tmsFeeRespVOS);
            }
            // 设置最新跟踪信息 1:1
            if (bo.getTracking() != null) {
                respVO.setVesselTracking(BeanUtils.toBean(bo.getTracking(), TmsVesselTrackingRespVO.class, peek -> {

                }));
            }
            return respVO;
        }).toList();
    }

    private Long safeParseLong(String value) {
        try {
            return Optional.ofNullable(value).map(Long::parseLong).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}