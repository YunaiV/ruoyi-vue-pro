package cn.iocoder.yudao.module.mes.controller.admin.pro.route;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.process.MesProRouteProcessRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.process.MesProRouteProcessSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 工艺路线工序")
@RestController
@RequestMapping("/mes/pro/route-process")
@Validated
public class MesProRouteProcessController {

    @Resource
    private MesProRouteProcessService routeProcessService;

    @Resource
    private MesProProcessService processService;

    @PostMapping("/create")
    @Operation(summary = "创建工艺路线工序")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Long> createRouteProcess(@Valid @RequestBody MesProRouteProcessSaveReqVO createReqVO) {
        return success(routeProcessService.createRouteProcess(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工艺路线工序")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Boolean> updateRouteProcess(@Valid @RequestBody MesProRouteProcessSaveReqVO updateReqVO) {
        routeProcessService.updateRouteProcess(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工艺路线工序")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Boolean> deleteRouteProcess(@RequestParam("id") Long id) {
        routeProcessService.deleteRouteProcess(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工艺路线工序")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<MesProRouteProcessRespVO> getRouteProcess(@RequestParam("id") Long id) {
        MesProRouteProcessDO routeProcess = routeProcessService.getRouteProcess(id);
        return success(buildRouteProcessRespVO(routeProcess));
    }

    // TODO @芋艿：到底叫 list-by-item 还是 list-by-product？
    @GetMapping("/list-by-product")
    @Operation(summary = "按产品获得工序列表", description = "根据产品查找关联的工艺路线，返回该路线的工序列表")
    @Parameter(name = "productId", description = "产品编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<List<MesProRouteProcessRespVO>> getRouteProcessListByProduct(@RequestParam("productId") Long productId) {
        List<MesProRouteProcessDO> list = routeProcessService.getRouteProcessListByProductId(productId);
        return success(buildRouteProcessRespVOList(list));
    }

    @GetMapping("/list-by-route")
    @Operation(summary = "按工艺路线获得工序列表")
    @Parameter(name = "routeId", description = "工艺路线编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<List<MesProRouteProcessRespVO>> getRouteProcessListByRoute(@RequestParam("routeId") Long routeId) {
        List<MesProRouteProcessDO> list = routeProcessService.getRouteProcessListByRouteId(routeId);
        return success(buildRouteProcessRespVOList(list));
    }

    @GetMapping("/get-by-route-and-process")
    @Operation(summary = "按工艺路线+工序获得工序配置")
    @Parameter(name = "routeId", description = "工艺路线编号", required = true, example = "1")
    @Parameter(name = "processId", description = "工序编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<MesProRouteProcessRespVO> getRouteProcessByRouteAndProcess(
            @RequestParam("routeId") Long routeId,
            @RequestParam("processId") Long processId) {
        MesProRouteProcessDO routeProcess = routeProcessService.getRouteProcessByRouteIdAndProcessId(routeId, processId);
        return success(buildRouteProcessRespVO(routeProcess));
    }

    // ==================== 拼接 VO ====================

    private List<MesProRouteProcessRespVO> buildRouteProcessRespVOList(List<MesProRouteProcessDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Set<Long> processIds = convertSet(list, MesProRouteProcessDO::getProcessId);
        list.forEach(item -> { if (item.getNextProcessId() != null) processIds.add(item.getNextProcessId()); });
        Map<Long, MesProProcessDO> processMap = convertMap(
                processService.getProcessList(new ArrayList<>(processIds)), MesProProcessDO::getId);
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesProRouteProcessRespVO.class, vo -> {
            MapUtils.findAndThen(processMap, vo.getProcessId(),
                    process -> vo.setProcessCode(process.getCode()).setProcessName(process.getName()));
            MapUtils.findAndThen(processMap, vo.getNextProcessId(),
                    nextProcess -> vo.setNextProcessName(nextProcess.getName()));
        });
    }

    private MesProRouteProcessRespVO buildRouteProcessRespVO(MesProRouteProcessDO routeProcess) {
        if (routeProcess == null) {
            return null;
        }
        return buildRouteProcessRespVOList(ListUtil.of(routeProcess)).get(0);
    }

}
