package cn.iocoder.yudao.module.mes.controller.admin.cal.plan;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.team.MesCalPlanTeamRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.team.MesCalPlanTeamSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanTeamService;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - MES 计划班组关联")
@RestController
@RequestMapping("/mes/cal/plan-team")
@Validated
public class MesCalPlanTeamController {

    @Resource
    private MesCalPlanTeamService planTeamService;
    @Resource
    private MesCalTeamService teamService;

    @PostMapping("/create")
    @Operation(summary = "创建计划班组关联")
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:update')")
    public CommonResult<Long> createPlanTeam(@Valid @RequestBody MesCalPlanTeamSaveReqVO createReqVO) {
        return success(planTeamService.createPlanTeam(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除计划班组关联")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:update')")
    public CommonResult<Boolean> deletePlanTeam(@RequestParam("id") Long id) {
        planTeamService.deletePlanTeam(id);
        return success(true);
    }

    @GetMapping("/list-by-plan")
    @Operation(summary = "获得指定排班计划的班组列表")
    @Parameter(name = "planId", description = "排班计划编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:cal-plan:query')")
    public CommonResult<List<MesCalPlanTeamRespVO>> getPlanTeamListByPlan(@RequestParam("planId") Long planId) {
        List<MesCalPlanTeamDO> list = planTeamService.getPlanTeamListByPlanId(planId);
        List<MesCalPlanTeamRespVO> respList = BeanUtils.toBean(list, MesCalPlanTeamRespVO.class);
        // 拼装班组编码/名称
        // TODO @AI：if return
        if (CollUtil.isNotEmpty(respList)) {
            Map<Long, MesCalTeamDO> teamMap = teamService.getTeamMap(
                    convertList(respList, MesCalPlanTeamRespVO::getTeamId));
            respList.forEach(resp -> {
                // TODO @AI：findand then
                MesCalTeamDO team = teamMap.get(resp.getTeamId());
                if (team != null) {
                    resp.setTeamCode(team.getCode()).setTeamName(team.getName());
                }
            });
        }
        return success(respList);
    }

}
