package cn.iocoder.yudao.module.mes.controller.admin.wm.batch;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchRespVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.tm.tool.MesTmToolService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 批次管理")
@RestController
@RequestMapping("/mes/wm/batch")
@Validated
public class MesWmBatchController {

    @Resource
    private MesWmBatchService batchService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;
    @Resource
    private MesMdVendorService vendorService;
    @Resource
    private MesMdClientService clientService;
    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesProTaskService taskService;
    @Resource
    private MesMdWorkstationService workstationService;
    @Resource
    private MesTmToolService toolService;

    @GetMapping("/get")
    @Operation(summary = "获得批次详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-batch:query')")
    public CommonResult<MesWmBatchRespVO> getBatch(@RequestParam("id") Long id) {
        MesWmBatchDO batch = batchService.getBatch(id);
        return success(buildBatchRespVO(batch));
    }

    @GetMapping("/page")
    @Operation(summary = "获得批次分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-batch:query')")
    public CommonResult<PageResult<MesWmBatchRespVO>> getBatchPage(@Valid MesWmBatchPageReqVO pageReqVO) {
        PageResult<MesWmBatchDO> pageResult = batchService.getBatchPage(pageReqVO);
        return success(new PageResult<>(buildBatchRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/forward-list")
    @Operation(summary = "批次向前追溯")
    @Parameter(name = "code", description = "批次编码", required = true, example = "BATCH20250314001")
    @PreAuthorize("@ss.hasPermission('mes:wm-batch:query')")
    public CommonResult<List<MesWmBatchRespVO>> getForwardList(@RequestParam("code") @Valid String code) {
        List<MesWmBatchDO> list = batchService.getForwardBatchList(code);
        return success(buildBatchRespVOList(list));
    }

    @GetMapping("/backward-list")
    @Operation(summary = "批次向后追溯")
    @Parameter(name = "code", description = "批次编码", required = true, example = "BATCH20250314001")
    @PreAuthorize("@ss.hasPermission('mes:wm-batch:query')")
    public CommonResult<List<MesWmBatchRespVO>> getBackwardList(@RequestParam("code") @Valid String code) {
        List<MesWmBatchDO> list = batchService.getBackwardBatchList(code);
        return success(buildBatchRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmBatchRespVO> buildBatchRespVOList(List<MesWmBatchDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmBatchDO::getItemId));
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesWmBatchDO::getVendorId));
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesWmBatchDO::getClientId));
        Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesWmBatchDO::getWorkOrderId));
        Map<Long, MesProTaskDO> taskMap = taskService.getTaskMap(
                convertSet(list, MesWmBatchDO::getTaskId));
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(list, MesWmBatchDO::getWorkstationId));
        Map<Long, MesTmToolDO> toolMap = toolService.getToolMap(
                convertSet(list, MesWmBatchDO::getToolId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmBatchRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName())
                        .setItemSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitName(unitMeasure.getName()));
            });
            MapUtils.findAndThen(vendorMap, vo.getVendorId(),
                    vendor -> vo.setVendorCode(vendor.getCode()).setVendorName(vendor.getName()));
            MapUtils.findAndThen(clientMap, vo.getClientId(),
                    client -> vo.setClientCode(client.getCode()).setClientName(client.getName()));
            MapUtils.findAndThen(workOrderMap, vo.getWorkOrderId(),
                    workOrder -> vo.setWorkOrderCode(workOrder.getCode()));
            MapUtils.findAndThen(taskMap, vo.getTaskId(),
                    task -> vo.setTaskCode(task.getCode()));
            MapUtils.findAndThen(workstationMap, vo.getWorkstationId(),
                    workstation -> vo.setWorkstationCode(workstation.getCode()));
            MapUtils.findAndThen(toolMap, vo.getToolId(),
                    tool -> vo.setToolCode(tool.getCode()));
        });
    }

    private MesWmBatchRespVO buildBatchRespVO(MesWmBatchDO batch) {
        if (batch == null) {
            return null;
        }
        return buildBatchRespVOList(Collections.singletonList(batch)).get(0);
    }

}
