package cn.iocoder.yudao.module.pms.controller.admin.pm.iteration;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationStartReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 项目迭代")
@RestController
@RequestMapping("/pms/pm/iteration")
@Validated
public class PmsIterationController {

    @Resource
    private PmsIterationService iterationService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建项目迭代")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:create')")
    public CommonResult<Long> createIteration(@Valid @RequestBody PmsIterationSaveReqVO saveReqVO) {
        return success(iterationService.createIteration(saveReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目迭代")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:update')")
    public CommonResult<Boolean> updateIteration(@Valid @RequestBody PmsIterationSaveReqVO saveReqVO) {
        iterationService.updateIteration(saveReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/start")
    @Operation(summary = "开始项目迭代")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:update')")
    public CommonResult<Boolean> startIteration(@Valid @RequestBody PmsIterationStartReqVO startReqVO) {
        iterationService.startIteration(startReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成项目迭代")
    @Parameter(name = "id", description = "迭代编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:update')")
    public CommonResult<Boolean> completeIteration(@RequestParam("id") Long id) {
        iterationService.completeIteration(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目迭代")
    @Parameter(name = "id", description = "迭代编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:delete')")
    public CommonResult<Boolean> deleteIteration(@RequestParam("id") Long id) {
        iterationService.deleteIteration(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目迭代详情")
    @Parameter(name = "id", description = "迭代编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:query')")
    public CommonResult<PmsIterationRespVO> getIteration(@RequestParam("id") Long id) {
        PmsIterationDO iteration = iterationService.getIteration(id, getLoginUserId());
        PmsIterationRespVO iterationVO = buildIterationRespVO(iteration);
        return success(iterationVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目迭代分页")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:query')")
    public CommonResult<PageResult<PmsIterationRespVO>> getIterationPage(
            @Valid PmsIterationPageReqVO pageReqVO) {
        PageResult<PmsIterationDO> pageResult = iterationService.getIterationPage(pageReqVO, getLoginUserId());
        List<PmsIterationRespVO> iterationVOList = buildIterationRespVOList(pageResult.getList());
        Map<Long, Integer> progressMap = iterationService.getIterationProgressMap(
                convertSet(pageResult.getList(), PmsIterationDO::getId));
        iterationVOList.forEach(iteration -> iteration.setProgress(progressMap.getOrDefault(iteration.getId(), 0)));
        return success(new PageResult<>(iterationVOList, pageResult.getTotal()));
    }

    @GetMapping("/overview")
    @Operation(summary = "获得迭代概览统计")
    @Parameter(name = "id", description = "迭代编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:iteration:query')")
    public CommonResult<PmsIterationOverviewRespVO> getIterationOverview(@RequestParam("id") Long id) {
        PmsIterationOverviewRespVO overview = iterationService.getIterationOverview(id, getLoginUserId());
        List<PmsIterationOverviewRespVO.ActivityItem> activities = overview.getRecentActivities();
        Map<Long, AdminUserRespDTO> activityUserMap = adminUserApi.getUserMap(
                convertSet(activities, PmsIterationOverviewRespVO.ActivityItem::getOperatorUserId));
        activities.forEach(activity -> findAndThen(activityUserMap, activity.getOperatorUserId(),
                user -> activity.setOperatorUserName(user.getNickname())));
        return success(overview.setRecentActivities(activities));
    }

    // ==================== 拼接 VO ====================

    private PmsIterationRespVO buildIterationRespVO(PmsIterationDO iteration) {
        return CollUtil.getFirst(buildIterationRespVOList(Collections.singletonList(iteration)));
    }

    private List<PmsIterationRespVO> buildIterationRespVOList(List<PmsIterationDO> iterations) {
        if (CollUtil.isEmpty(iterations)) {
            return Collections.emptyList();
        }

        // 1. 批量查询负责人
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(iterations, PmsIterationDO::getOwnerUserId));

        // 2. 转换迭代响应，并拼接负责人姓名
        return BeanUtils.toBean(iterations, PmsIterationRespVO.class, iterationVO ->
                findAndThen(userMap, iterationVO.getOwnerUserId(),
                        user -> iterationVO.setOwnerUserName(user.getNickname())));
    }

}
