package cn.iocoder.yudao.module.mes.controller.admin.md.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.machine.MesMdWorkstationMachineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.machine.MesMdWorkstationMachineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationMachineDO;
import cn.iocoder.yudao.module.mes.service.dv.machinery.MesDvMachineryService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationMachineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 设备资源")
@RestController
@RequestMapping("/mes/md-workstation-machine")
@Validated
public class MesMdWorkstationMachineController {

    @Resource
    private MesMdWorkstationMachineService workstationMachineService;

    @Resource
    private MesDvMachineryService machineryService;

    @PostMapping("/create")
    @Operation(summary = "创建设备资源")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Long> createWorkstationMachine(@Valid @RequestBody MesMdWorkstationMachineSaveReqVO createReqVO) {
        return success(workstationMachineService.createWorkstationMachine(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备资源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Boolean> deleteWorkstationMachine(@RequestParam("id") Long id) {
        workstationMachineService.deleteWorkstationMachine(id);
        return success(true);
    }

    @GetMapping("/list-by-workstation")
    @Operation(summary = "获得设备资源列表")
    @Parameter(name = "workstationId", description = "工作站编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:query')")
    public CommonResult<List<MesMdWorkstationMachineRespVO>> getWorkstationMachineList(
            @RequestParam("workstationId") Long workstationId) {
        List<MesMdWorkstationMachineDO> list = workstationMachineService.getWorkstationMachineListByWorkstationId(workstationId);
        return success(buildWorkstationMachineRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesMdWorkstationMachineRespVO> buildWorkstationMachineRespVOList(List<MesMdWorkstationMachineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取设备信息
        Map<Long, MesDvMachineryDO> machineryMap = machineryService.getMachineryMap(
                convertSet(list, MesMdWorkstationMachineDO::getMachineryId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesMdWorkstationMachineRespVO.class, vo -> {
            MapUtils.findAndThen(machineryMap, vo.getMachineryId(),
                    machinery -> vo.setMachineryCode(machinery.getCode()).setMachineryName(machinery.getName()));
        });
    }

}
