package cn.iocoder.yudao.module.mes.controller.admin.pro.workorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
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
import java.util.*;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 生产工单")
@RestController
@RequestMapping("/mes/pro/work-order")
@Validated
public class MesProWorkOrderController {

    @Resource
    private MesProWorkOrderService workOrderService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdClientService clientService;

    @Resource
    private MesMdVendorService vendorService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建生产工单")
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:create')")
    public CommonResult<Long> createWorkOrder(@Valid @RequestBody MesProWorkOrderSaveReqVO createReqVO) {
        return success(workOrderService.createWorkOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产工单")
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:update')")
    public CommonResult<Boolean> updateWorkOrder(@Valid @RequestBody MesProWorkOrderSaveReqVO updateReqVO) {
        workOrderService.updateWorkOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产工单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:delete')")
    public CommonResult<Boolean> deleteWorkOrder(@RequestParam("id") Long id) {
        workOrderService.deleteWorkOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:query')")
    public CommonResult<MesProWorkOrderRespVO> getWorkOrder(@RequestParam("id") Long id) {
        MesProWorkOrderDO workOrder = workOrderService.getWorkOrder(id);
        if (workOrder == null) {
            return success(null);
        }
        return success(buildWorkOrderRespVOList(ListUtil.of(workOrder)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产工单分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:query')")
    public CommonResult<PageResult<MesProWorkOrderRespVO>> getWorkOrderPage(@Valid MesProWorkOrderPageReqVO pageReqVO) {
        PageResult<MesProWorkOrderDO> pageResult = workOrderService.getWorkOrderPage(pageReqVO);
        return success(new PageResult<>(buildWorkOrderRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产工单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWorkOrderExcel(@Valid MesProWorkOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesProWorkOrderDO> list = workOrderService.getWorkOrderPage(pageReqVO).getList();
        List<MesProWorkOrderRespVO> voList = buildWorkOrderRespVOList(list);
        ExcelUtils.write(response, "生产工单.xls", "数据", MesProWorkOrderRespVO.class, voList);
    }

    @PutMapping("/confirm")
    @Operation(summary = "确认工单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:update')")
    public CommonResult<Boolean> confirmWorkOrder(@RequestParam("id") Long id) {
        workOrderService.confirmWorkOrder(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成工单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:update')")
    public CommonResult<Boolean> finishWorkOrder(@RequestParam("id") Long id) {
        workOrderService.finishWorkOrder(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消工单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-work-order:update')")
    public CommonResult<Boolean> cancelWorkOrder(@RequestParam("id") Long id) {
        workOrderService.cancelWorkOrder(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesProWorkOrderRespVO> buildWorkOrderRespVOList(List<MesProWorkOrderDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesProWorkOrderDO::getProductId));
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesProWorkOrderDO::getClientId));
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesProWorkOrderDO::getVendorId));
        // 单位从产品关联的 item 获得
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesProWorkOrderDO> parentMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesProWorkOrderDO::getParentId));
        // 2. 拼接 VO（单位名称从产品关联的 item 获得）
        return BeanUtils.toBean(list, MesProWorkOrderRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getProductId(), item -> {
                vo.setProductName(item.getName()).setProductCode(item.getCode())
                        .setProductSpec(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
            MapUtils.findAndThen(clientMap, vo.getClientId(),
                    client -> vo.setClientName(client.getName()).setClientCode(client.getCode()));
            MapUtils.findAndThen(vendorMap, vo.getVendorId(),
                    vendor -> vo.setVendorName(vendor.getName()).setVendorCode(vendor.getCode()));
            MapUtils.findAndThen(parentMap, vo.getParentId(),
                    parent -> vo.setParentCode(parent.getCode()));
        });
    }

}
