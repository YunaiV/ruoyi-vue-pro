package cn.iocoder.yudao.module.mes.controller.admin.pro.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskIssueRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskIssueDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.pro.task.MesProTaskIssueService;
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
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

// TODO @芋艿：【对齐】前端没地方调用，这里只先写后端代码；
@Tag(name = "管理后台 - MES 生产任务投料")
@RestController
@RequestMapping("/mes/pro/task-issue")
@Validated
public class MesProTaskIssueController {

    @Resource
    private MesProTaskIssueService taskIssueService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建生产任务投料")
    @PreAuthorize("@ss.hasPermission('mes:pro-task-issue:create')")
    public CommonResult<Long> createTaskIssue(@Valid @RequestBody MesProTaskIssueSaveReqVO createReqVO) {
        return success(taskIssueService.createTaskIssue(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产任务投料")
    @PreAuthorize("@ss.hasPermission('mes:pro-task-issue:update')")
    public CommonResult<Boolean> updateTaskIssue(@Valid @RequestBody MesProTaskIssueSaveReqVO updateReqVO) {
        taskIssueService.updateTaskIssue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产任务投料")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-task-issue:delete')")
    public CommonResult<Boolean> deleteTaskIssue(@RequestParam("id") Long id) {
        taskIssueService.deleteTaskIssue(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产任务投料")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-task-issue:query')")
    public CommonResult<MesProTaskIssueRespVO> getTaskIssue(@RequestParam("id") Long id) {
        MesProTaskIssueDO taskIssue = taskIssueService.getTaskIssue(id);
        if (taskIssue == null) {
            return success(null);
        }
        return success(buildIssueRespVOList(ListUtil.of(taskIssue)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产任务投料分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-task-issue:query')")
    public CommonResult<PageResult<MesProTaskIssueRespVO>> getTaskIssuePage(@Valid MesProTaskIssuePageReqVO pageReqVO) {
        PageResult<MesProTaskIssueDO> pageResult = taskIssueService.getTaskIssuePage(pageReqVO);
        return success(new PageResult<>(buildIssueRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/list-by-task")
    @Operation(summary = "根据任务获得投料列表")
    @Parameter(name = "taskId", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-task-issue:query')")
    public CommonResult<List<MesProTaskIssueRespVO>> getTaskIssueListByTask(@RequestParam("taskId") Long taskId) {
        List<MesProTaskIssueDO> list = taskIssueService.getTaskIssueListByTaskId(taskId);
        return success(buildIssueRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesProTaskIssueRespVO> buildIssueRespVOList(List<MesProTaskIssueDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量查询关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesProTaskIssueDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMap = unitMeasureService.getUnitMeasureMap(
                convertSet(list, MesProTaskIssueDO::getUnitMeasureId));
        // 拼接 VO
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList(list, issue -> {
            MesProTaskIssueRespVO vo = BeanUtils.toBean(issue, MesProTaskIssueRespVO.class);
            findAndThen(itemMap, issue.getItemId(), item ->
                    vo.setItemCode(item.getCode()).setItemName(item.getName()).setItemSpec(item.getSpecification()));
            findAndThen(unitMap, issue.getUnitMeasureId(), unit ->
                    vo.setUnitMeasureName(unit.getName()));
            return vo;
        });
    }

}
