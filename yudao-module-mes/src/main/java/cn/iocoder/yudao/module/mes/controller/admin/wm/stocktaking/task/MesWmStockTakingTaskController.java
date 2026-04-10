package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.*;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskDO;
import cn.iocoder.yudao.module.mes.service.wm.stocktaking.plan.MesWmStockTakingPlanService;
import cn.iocoder.yudao.module.mes.service.wm.stocktaking.task.MesWmStockTakingTaskService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 盘点任务")
@RestController
@RequestMapping("/mes/wm/stocktaking-task")
@Validated
public class MesWmStockTakingTaskController {

    @Resource
    private MesWmStockTakingTaskService stockTakingTaskService;
    @Resource
    private MesWmStockTakingPlanService stockTakingPlanService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建盘点任务")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:create')")
    public CommonResult<Long> createStockTakingTask(@Valid @RequestBody MesWmStockTakingTaskSaveReqVO createReqVO) {
        return success(stockTakingTaskService.createStockTakingTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改盘点任务")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    public CommonResult<Boolean> updateStockTakingTask(@Valid @RequestBody MesWmStockTakingTaskSaveReqVO updateReqVO) {
        stockTakingTaskService.updateStockTakingTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:delete')")
    public CommonResult<Boolean> deleteStockTakingTask(@RequestParam("id") Long id) {
        stockTakingTaskService.deleteStockTakingTask(id);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交盘点任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    public CommonResult<Boolean> submitStockTakingTask(@RequestParam("id") Long id) {
        stockTakingTaskService.submitStockTakingTask(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得盘点任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:query')")
    public CommonResult<MesWmStockTakingTaskRespVO> getStockTakingTask(@RequestParam("id") Long id) {
        MesWmStockTakingTaskDO task = stockTakingTaskService.getStockTakingTask(id);
        if (task == null) {
            return success(null);
        }
        return success(buildTaskRespVOList(Collections.singletonList(task)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘点任务分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:query')")
    public CommonResult<PageResult<MesWmStockTakingTaskRespVO>> getStockTakingTaskPage(@Valid MesWmStockTakingTaskPageReqVO pageReqVO) {
        PageResult<MesWmStockTakingTaskDO> pageResult = stockTakingTaskService.getStockTakingTaskPage(pageReqVO);
        return success(new PageResult<>(buildTaskRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出盘点任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockTakingTaskExcel(@Valid MesWmStockTakingTaskPageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmStockTakingTaskDO> pageResult = stockTakingTaskService.getStockTakingTaskPage(pageReqVO);
        ExcelUtils.write(response, "盘点任务.xls", "数据", MesWmStockTakingTaskRespVO.class,
                buildTaskRespVOList(pageResult.getList()));
    }

    @PutMapping("/finish")
    @Operation(summary = "完成盘点任务")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:finish')")
    public CommonResult<Boolean> finishStockTakingTask(@Valid @RequestBody MesWmStockTakingTaskFinishReqVO reqVO) {
        stockTakingTaskService.finishStockTakingTask(reqVO.getId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消盘点任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-task:update')")
    public CommonResult<Boolean> cancelStockTakingTask(@RequestParam("id") Long id) {
        stockTakingTaskService.cancelStockTakingTask(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesWmStockTakingTaskRespVO> buildTaskRespVOList(List<MesWmStockTakingTaskDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询关联数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesWmStockTakingTaskDO::getUserId));
        Map<Long, MesWmStockTakingPlanDO> planMap = stockTakingPlanService.getStockTakingPlanMap(
                convertSet(list, MesWmStockTakingTaskDO::getPlanId));
        // 拼接数据
        return BeanUtils.toBean(list, MesWmStockTakingTaskRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getUserId(),
                    user -> vo.setUserNickname(user.getNickname()));
            MapUtils.findAndThen(planMap, vo.getPlanId(),
                    plan -> vo.setPlanCode(plan.getCode()).setPlanName(plan.getName()));
        });
    }

}
