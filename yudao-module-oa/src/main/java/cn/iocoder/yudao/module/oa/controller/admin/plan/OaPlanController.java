package cn.iocoder.yudao.module.oa.controller.admin.plan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanCommentReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanReportPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanReportRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.plan.OaPlanDO;
import cn.iocoder.yudao.module.oa.service.plan.OaPlanService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 工作计划")
@RestController
@RequestMapping("/oa/plan")
@Validated
public class OaPlanController {

    @Resource
    private OaPlanService planService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    // ==================== 我的计划 ====================

    @PostMapping("/create")
    @Operation(summary = "创建工作计划")
    @PreAuthorize("@ss.hasPermission('oa:plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody OaPlanSaveReqVO createReqVO) {
        return success(planService.createPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作计划")
    @PreAuthorize("@ss.hasPermission('oa:plan:update')")
    public CommonResult<Boolean> updatePlan(@Valid @RequestBody OaPlanSaveReqVO updateReqVO) {
        planService.updatePlan(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作计划")
    @Parameter(name = "id", description = "计划编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:plan:delete')")
    public CommonResult<Boolean> deletePlan(@RequestParam("id") Long id) {
        planService.deletePlan(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作计划详情")
    @Parameter(name = "id", description = "计划编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:plan:query')")
    public CommonResult<OaPlanRespVO> getPlan(@RequestParam("id") Long id) {
        OaPlanDO plan = planService.getPlan(id, getLoginUserId());
        return success(buildPlanRespVO(plan));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工作计划分页")
    @PreAuthorize("@ss.hasPermission('oa:plan:query')")
    public CommonResult<PageResult<OaPlanRespVO>> getPlanPage(@Valid OaPlanPageReqVO pageReqVO) {
        PageResult<OaPlanDO> pageResult = planService.getPlanPage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildPlanRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 计划报表 ====================

    @GetMapping("/report-page")
    @Operation(summary = "获得工作计划报表分页")
    @PreAuthorize("@ss.hasPermission('oa:plan:query')")
    public CommonResult<PageResult<OaPlanReportRespVO>> getPlanReportPage(
            @Valid OaPlanReportPageReqVO pageReqVO) {
        // 1.1 查询 System 管理范围内的用户
        List<AdminUserRespDTO> users = adminUserApi.getUserListBySubordinate(getLoginUserId());
        // 1.2 按成员姓名筛选，按用户编号保持分页稳定
        users = convertList(users, Function.identity(), user -> StrUtil.isBlank(pageReqVO.getUserName())
                || StrUtil.containsIgnoreCase(StrUtil.nullToEmpty(user.getNickname()), pageReqVO.getUserName()));
        users.sort(Comparator.comparing(AdminUserRespDTO::getId));

        // 2.1 对成员分页
        PageResult<AdminUserRespDTO> userPage = PageUtils.buildPageResult(pageReqVO, users);
        List<AdminUserRespDTO> pageUsers = userPage.getList();
        // 2.2 查询分页成员在统计周期内的最新计划
        Map<Long, OaPlanDO> latestPlanMap = planService.getLatestPlanMap(
                convertSet(pageUsers, AdminUserRespDTO::getId), pageReqVO.getType(), pageReqVO.getCreateTime());
        // 3. 拼接报表响应
        return success(new PageResult<>(buildPlanReportRespVOList(pageUsers, latestPlanMap), userPage.getTotal()));
    }

    @PutMapping("/add-comment")
    @Operation(summary = "点评工作计划")
    @PreAuthorize("@ss.hasPermission('oa:plan:comment')")
    public CommonResult<Boolean> addPlanComment(@Valid @RequestBody OaPlanCommentReqVO updateReqVO) {
        planService.addPlanComment(updateReqVO.getId(), updateReqVO.getComment(), getLoginUserId());
        return success(true);
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接工作计划响应
     *
     * @param plan 工作计划
     * @return 工作计划响应
     */
    private OaPlanRespVO buildPlanRespVO(OaPlanDO plan) {
        return CollUtil.getFirst(buildPlanRespVOList(Collections.singletonList(plan)));
    }

    /**
     * 拼接工作计划响应列表
     *
     * @param plans 工作计划列表
     * @return 工作计划响应列表
     */
    private List<OaPlanRespVO> buildPlanRespVOList(List<OaPlanDO> plans) {
        if (CollUtil.isEmpty(plans)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(plans, plan -> NumberUtils.parseLong(plan.getCreator())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return convertList(plans, plan -> {
            OaPlanRespVO respVO = BeanUtils.toBean(plan, OaPlanRespVO.class)
                    .setUserId(NumberUtils.parseLong(plan.getCreator()));
            MapUtils.findAndThen(userMap, respVO.getUserId(), user -> {
                respVO.setUserName(user.getNickname()).setDeptId(user.getDeptId());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            });
            return respVO;
        });
    }

    /**
     * 拼接工作计划报表响应列表
     *
     * @param users 成员用户列表
     * @param latestPlanMap 最新计划 Map
     * @return 工作计划报表响应列表
     */
    private List<OaPlanReportRespVO> buildPlanReportRespVOList(List<AdminUserRespDTO> users,
                                                               Map<Long, OaPlanDO> latestPlanMap) {
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(users, AdminUserRespDTO::getDeptId));
        return convertList(users, user -> {
            OaPlanDO plan = latestPlanMap.get(user.getId());
            OaPlanReportRespVO report = plan == null ? new OaPlanReportRespVO()
                    : BeanUtils.toBean(plan, OaPlanReportRespVO.class).setPlanId(plan.getId());
            report.setUserId(user.getId()).setUserName(user.getNickname()).setDeptId(user.getDeptId());
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> report.setDeptName(dept.getName()));
            return report;
        });
    }

}
