package cn.iocoder.yudao.module.mes.controller.admin.qc.pendinginspect;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.pendinginspect.vo.MesQcPendingInspectPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.pendinginspect.vo.MesQcPendingInspectRespVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskService;
import cn.iocoder.yudao.module.mes.service.qc.pendinginspect.MesQcPendingInspectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - MES 待检任务")
@RestController
@RequestMapping("/mes/qc/pending-inspect")
@Validated
public class MesQcPendingInspectController {

    @Resource
    private MesQcPendingInspectService pendingInspectService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;
    @Resource
    private MesMdVendorService vendorService;
    @Resource
    private MesMdClientService clientService;
    @Resource
    private MesMdWorkstationService workstationService;
    @Resource
    private MesProTaskService taskService;

    @GetMapping("/page")
    @Operation(summary = "获得待检任务分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-pending-inspect:query')")
    public CommonResult<PageResult<MesQcPendingInspectRespVO>> getPendingInspectPage(
            @Valid MesQcPendingInspectPageReqVO pageReqVO) {
        PageResult<MesQcPendingInspectRespVO> pageResult = pendingInspectService.getPendingInspectPage(pageReqVO);
        // 拼接 VO：物料、供应商、客户、工作站、任务等
        buildPendingInspectRespVOList(pageResult.getList());
        return success(pageResult);
    }

    // ==================== 拼接 VO ====================

    private void buildPendingInspectRespVOList(List<MesQcPendingInspectRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 批量查询物料
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesQcPendingInspectRespVO::getItemId));
        // 批量查询计量单位（通过物料的 unitMeasureId）
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 批量查询供应商（IQC 场景）
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesQcPendingInspectRespVO::getVendorId));
        // 批量查询客户（OQC/RQC 场景）
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesQcPendingInspectRespVO::getClientId));
        // 批量查询工作站（IPQC/RQC 场景）
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(list, MesQcPendingInspectRespVO::getWorkstationId));
        // 批量查询任务（IPQC 场景）
        Map<Long, MesProTaskDO> taskMap = taskService.getTaskMap(
                convertSet(list, MesQcPendingInspectRespVO::getTaskId));
        // 拼装
        list.forEach(vo -> {
            findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unit -> vo.setUnitName(unit.getName()));
            });
            findAndThen(vendorMap, vo.getVendorId(), vendor -> vo.setVendorName(vendor.getName()));
            findAndThen(clientMap, vo.getClientId(), client -> vo.setClientName(client.getName()));
            findAndThen(workstationMap, vo.getWorkstationId(),
                    workstation -> vo.setWorkstationName(workstation.getName()));
            findAndThen(taskMap, vo.getTaskId(), task -> vo.setTaskCode(task.getCode()));
        });
    }

}
