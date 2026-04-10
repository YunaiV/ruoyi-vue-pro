package cn.iocoder.yudao.module.mes.controller.admin.pro.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.GanttDataRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskSaveReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskService;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - MES 生产任务")
@RestController
@RequestMapping("/mes/pro/task")
@Validated
public class MesProTaskController {

    @Resource
    private MesProTaskService taskService;

    @Resource
    private MesProWorkOrderService workOrderService;

    @Resource
    private MesMdWorkstationService workstationService;

    @Resource
    private MesProProcessService processService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdClientService clientService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建生产任务")
    @PreAuthorize("@ss.hasPermission('mes:pro-task:create')")
    public CommonResult<Long> createTask(@Valid @RequestBody MesProTaskSaveReqVO createReqVO) {
        return success(taskService.createTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产任务")
    @PreAuthorize("@ss.hasPermission('mes:pro-task:update')")
    public CommonResult<Boolean> updateTask(@Valid @RequestBody MesProTaskSaveReqVO updateReqVO) {
        taskService.updateTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-task:delete')")
    public CommonResult<Boolean> deleteTask(@RequestParam("id") Long id) {
        taskService.deleteTask(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-task:query')")
    public CommonResult<MesProTaskRespVO> getTask(@RequestParam("id") Long id) {
        MesProTaskDO task = taskService.getTask(id);
        if (task == null) {
            return success(null);
        }
        return success(buildTaskRespVOList(ListUtil.of(task)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产任务分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-task:query')")
    public CommonResult<PageResult<MesProTaskRespVO>> getTaskPage(@Valid MesProTaskPageReqVO pageReqVO) {
        PageResult<MesProTaskDO> pageResult = taskService.getTaskPage(pageReqVO);
        return success(new PageResult<>(buildTaskRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得生产任务精简列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<MesProTaskRespVO>> getTaskSimpleList(
            @RequestParam(value = "workOrderId", required = false) Long workOrderId) {
        List<MesProTaskDO> list = taskService.getTaskListByWorkOrderId(workOrderId);
        return success(convertList(list, task -> new MesProTaskRespVO()
                .setId(task.getId()).setCode(task.getCode()).setName(task.getName())
                .setWorkOrderId(task.getWorkOrderId()).setWorkstationId(task.getWorkstationId())
                .setRouteId(task.getRouteId()).setProcessId(task.getProcessId())
                .setItemId(task.getItemId()).setStatus(task.getStatus())));
    }

    @GetMapping("/gantt-list")
    @Operation(summary = "获得甘特图任务列表", description = "后端组装工单=project + 任务=task 列表")
    @PreAuthorize("@ss.hasPermission('mes:pro-task:query')")
    public CommonResult<List<GanttDataRespVO>> listGanttTaskList(@Valid MesProWorkOrderPageReqVO reqVO) {
        // 1.1 查询匹配的工单（不分页）
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesProWorkOrderDO> workOrders = workOrderService.getWorkOrderPage(reqVO).getList();
        if (CollUtil.isEmpty(workOrders)) {
            return success(Collections.emptyList());
        }
        // 1.2 批量查询所有工单下的任务，按 workOrderId 分组
        List<MesProTaskDO> allTasks = taskService.getTaskListByWorkOrderIds(
                convertSet(workOrders, MesProWorkOrderDO::getId));
        Map<Long, List<MesProTaskDO>> taskMap = convertMultiMap(allTasks, MesProTaskDO::getWorkOrderId);

        // 2.1 查询关联数据
        java.util.Set<Long> allItemIds = new java.util.HashSet<>();
        allItemIds.addAll(convertSet(workOrders, MesProWorkOrderDO::getProductId));
        allItemIds.addAll(convertSet(allTasks, MesProTaskDO::getItemId));
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(allItemIds);
        Map<Long, MesMdUnitMeasureDO> unitMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(allTasks, MesProTaskDO::getWorkstationId));
        Map<Long, MesProProcessDO> processMap = processService.getProcessMap(
                convertSet(allTasks, MesProTaskDO::getProcessId));
        // 2.2 组装甘特图数据
        List<GanttDataRespVO> ganttData = new java.util.ArrayList<>();
        for (MesProWorkOrderDO workOrder : workOrders) {
            // 2.2.a 工单 -> project 行
            MesMdItemDO item = itemMap.get(workOrder.getProductId());
            String productName = item != null ? item.getName() : "";
            GanttDataRespVO woData = new GanttDataRespVO()
                    .setId(MesBizTypeConstants.PRO_WORKORDER + "_" + workOrder.getId())
                    .setOriginalId(workOrder.getId())
                    .setType(MesBizTypeConstants.PRO_WORKORDER)
                    .setText(buildGanttText(item, workOrder.getQuantity(), unitMap))
                    .setProduct(productName)
                    .setQuantity(workOrder.getQuantity())
                    .setDuration(0L)
                    .setProgress(calcProgress(workOrder.getQuantityProduced(), workOrder.getQuantity()));
            if (ObjUtil.notEqual(workOrder.getParentId(), MesProWorkOrderDO.PARENT_ID_NULL)) {
                woData.setParent(MesBizTypeConstants.PRO_WORKORDER + "_" + workOrder.getParentId());
            }
            ganttData.add(woData);

            // 2.2.b 任务 -> task 行
            List<MesProTaskDO> woTasks = taskMap.getOrDefault(workOrder.getId(), Collections.emptyList());
            for (MesProTaskDO task : woTasks) {
                MesMdWorkstationDO ws = workstationMap.get(task.getWorkstationId());
                MesProProcessDO proc = processMap.get(task.getProcessId());
                MesMdItemDO taskItem = itemMap.get(task.getItemId());
                GanttDataRespVO tData = new GanttDataRespVO()
                        .setId(MesBizTypeConstants.PRO_TASK + "_" + task.getId())
                        .setOriginalId(task.getId())
                        .setType(MesBizTypeConstants.PRO_TASK)
                        .setText(buildGanttText(taskItem, task.getQuantity(), unitMap))
                        .setParent(MesBizTypeConstants.PRO_WORKORDER + "_" + workOrder.getId())
                        .setWorkstation(ws != null ? ws.getName() : null)
                        .setProcess(proc != null ? proc.getName() : null)
                        .setColor(task.getColorCode())
                        .setQuantity(task.getQuantity())
                        .setStartDate(task.getStartTime()).setEndDate(task.getEndTime())
                        .setDuration(task.getDuration() != null ? task.getDuration().longValue() : null)
                        .setProgress(calcProgress(task.getProducedQuantity(), task.getQuantity()));
                ganttData.add(tData);
            }
        }
        return success(ganttData);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:pro-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTaskExcel(@Valid MesProTaskPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesProTaskDO> list = taskService.getTaskPage(pageReqVO).getList();
        List<MesProTaskRespVO> voList = buildTaskRespVOList(list);
        ExcelUtils.write(response, "生产任务.xls", "数据", MesProTaskRespVO.class, voList);
    }

    // ==================== 拼接 VO ====================

    private List<MesProTaskRespVO> buildTaskRespVOList(List<MesProTaskDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量查询关联数据
        Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesProTaskDO::getWorkOrderId));
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(list, MesProTaskDO::getWorkstationId));
        Map<Long, MesProProcessDO> processMap = processService.getProcessMap(
                new java.util.ArrayList<>(convertSet(list, MesProTaskDO::getProcessId)));
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesProTaskDO::getItemId));
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesProTaskDO::getClientId));
        // 拼接 VO
        return convertList(list, task -> {
            MesProTaskRespVO vo = BeanUtils.toBean(task, MesProTaskRespVO.class);
            findAndThen(workOrderMap, task.getWorkOrderId(), wo ->
                    vo.setWorkOrderCode(wo.getCode()).setWorkOrderName(wo.getName()).setRequestDate(wo.getRequestDate()));
            findAndThen(workstationMap, task.getWorkstationId(), ws ->
                    vo.setWorkstationCode(ws.getCode()).setWorkstationName(ws.getName()));
            findAndThen(processMap, task.getProcessId(), p ->
                    vo.setProcessName(p.getName()));
            findAndThen(itemMap, task.getItemId(), item ->
                    vo.setItemCode(item.getCode()).setItemName(item.getName()).setItemSpec(item.getSpecification()));
            findAndThen(clientMap, task.getClientId(), c ->
                    vo.setClientName(c.getName()));
            return vo;
        });
    }

    /**
     * 拼接甘特图显示文本，格式："[产品名][数量][单位]"
     */
    private String buildGanttText(MesMdItemDO item, BigDecimal quantity,
                                  Map<Long, MesMdUnitMeasureDO> unitMap) {
        String itemName = item != null ? item.getName() : "";
        String quantityStr = quantity != null ? quantity.stripTrailingZeros().toPlainString() : "";
        String unitName = "";
        if (item != null && item.getUnitMeasureId() != null) {
            MesMdUnitMeasureDO unit = unitMap.get(item.getUnitMeasureId());
            unitName = unit != null ? unit.getName() : "";
        }
        return itemName + quantityStr + unitName;
    }

    /**
     * 计算进度 = 已生产 / 总量，返回 0~1
     */
    private float calcProgress(BigDecimal produced, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0 || produced == null) {
            return 0f;
        }
        return produced.divide(total, RoundingMode.HALF_UP).floatValue();
    }

}
