package cn.iocoder.yudao.module.mes.controller.admin.cal.team;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.shift.MesCalTeamShiftListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.shift.MesCalTeamShiftRespVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamShiftDO;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanShiftService;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamService;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 班组排班")
@RestController
@RequestMapping("/mes/cal/team-shift")
@Validated
public class MesCalTeamShiftController {

    @Resource
    private MesCalTeamShiftService teamShiftService;
    @Resource
    private MesCalTeamService teamService;
    @Resource
    private MesCalPlanShiftService planShiftService;

    @GetMapping("/list")
    @Operation(summary = "获得班组排班列表")
    @PreAuthorize("@ss.hasPermission('mes:cal-team-shift:query')")
    public CommonResult<List<MesCalTeamShiftRespVO>> getTeamShiftList(@Valid MesCalTeamShiftListReqVO reqVO) {
        List<MesCalTeamShiftDO> list = teamShiftService.getTeamShiftList(reqVO);
        if (CollUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }
        // 关联查询班组名称和班次名称
        Map<Long, MesCalTeamDO> teamMap = teamService.getTeamMap(
                convertSet(list, MesCalTeamShiftDO::getTeamId));
        Map<Long, MesCalPlanShiftDO> shiftMap = planShiftService.getPlanShiftMap(
                convertSet(list, MesCalTeamShiftDO::getShiftId));
        return success(BeanUtils.toBean(list, MesCalTeamShiftRespVO.class, vo -> {
            MapUtils.findAndThen(teamMap, vo.getTeamId(), team -> vo.setTeamName(team.getName()));
            MapUtils.findAndThen(shiftMap, vo.getShiftId(), shift -> vo.setShiftName(shift.getName()));
        }));
    }

}
