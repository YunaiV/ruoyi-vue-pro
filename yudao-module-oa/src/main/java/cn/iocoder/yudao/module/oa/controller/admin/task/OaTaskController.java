package cn.iocoder.yudao.module.oa.controller.admin.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskFeedbackReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskLogRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskReceiverRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskRankingRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskLogDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskReceiverDO;
import cn.iocoder.yudao.module.oa.service.task.OaTaskService;
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 任务")
@RestController
@RequestMapping("/oa/task")
@Validated
public class OaTaskController {

    @Resource
    private OaTaskService taskService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    // ==================== 我发布的 ====================

    @PostMapping("/create")
    @Operation(summary = "创建任务")
    @PreAuthorize("@ss.hasPermission('oa:task:create')")
    public CommonResult<Long> createTask(@Valid @RequestBody OaTaskSaveReqVO createReqVO) {
        return success(taskService.createTask(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新任务")
    @PreAuthorize("@ss.hasPermission('oa:task:update')")
    public CommonResult<Boolean> updateTask(@Valid @RequestBody OaTaskSaveReqVO updateReqVO) {
        taskService.updateTask(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除发布的任务")
    @Parameter(name = "id", description = "任务编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:task:delete')")
    public CommonResult<Boolean> deleteTask(@RequestParam("id") Long id) {
        taskService.deleteTask(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/published-page")
    @Operation(summary = "获得我发布的任务分页")
    @PreAuthorize("@ss.hasPermission('oa:task:query')")
    public CommonResult<PageResult<OaTaskRespVO>> getPublishedTaskPage(@Valid OaTaskPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<OaTaskDO> pageResult = taskService.getPublishedTaskPage(pageReqVO, userId);
        return success(new PageResult<>(buildTaskRespVOList(pageResult.getList(), Collections.emptyList(), userId),
                pageResult.getTotal()));
    }

    // ==================== 我的任务 ====================

    @DeleteMapping("/delete-received")
    @Operation(summary = "删除接收的任务")
    @Parameter(name = "id", description = "任务编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteReceivedTask(@RequestParam("id") Long id) {
        taskService.deleteReceivedTask(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/received-page")
    @Operation(summary = "获得我的任务分页")
    @PreAuthorize("@ss.hasPermission('oa:task:query')")
    public CommonResult<PageResult<OaTaskRespVO>> getReceivedTaskPage(@Valid OaTaskPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<OaTaskDO> pageResult = taskService.getReceivedTaskPage(pageReqVO, userId);
        return success(new PageResult<>(buildTaskRespVOList(pageResult.getList(), Collections.emptyList(), userId),
                pageResult.getTotal()));
    }

    // ==================== 公共操作 ====================

    @PostMapping("/feedback")
    @Operation(summary = "创建任务反馈")
    public CommonResult<Boolean> feedbackTask(
            @Valid @RequestBody OaTaskFeedbackReqVO createReqVO) {
        taskService.feedbackTask(createReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得任务详情")
    @Parameter(name = "id", description = "任务编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:task:query')")
    public CommonResult<OaTaskRespVO> getTask(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        OaTaskDO task = taskService.getTask(id, userId);
        List<OaTaskLogDO> logs = taskService.getTaskLogList(id);
        return success(buildTaskRespVO(task, logs, userId));
    }

    // ==================== 任务统计 ====================

    @GetMapping("/get-status-count")
    @Operation(summary = "获得我的任务状态统计")
    public CommonResult<Map<Integer, Long>> getTaskStatusCount() {
        return success(taskService.getTaskStatusCountMap(getLoginUserId()));
    }

    @GetMapping("/get-completed-ranking")
    @Operation(summary = "获得任务完成排行")
    public CommonResult<List<OaTaskRankingRespVO>> getCompletedTaskRanking() {
        // 1. 查询完成任务数量和用户信息
        Map<Long, Long> countMap = taskService.getCompletedTaskCountMapByUserId();
        if (CollUtil.isEmpty(countMap)) {
            return success(Collections.emptyList());
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(countMap.keySet());
        // 2. 拼接排行展示字段，保留 Service 返回的排行顺序
        return success(convertList(countMap.entrySet(), entry -> {
            OaTaskRankingRespVO ranking = new OaTaskRankingRespVO()
                    .setUserId(entry.getKey()).setCompletedCount(entry.getValue());
            MapUtils.findAndThen(userMap, entry.getKey(), user -> ranking.setUserName(user.getNickname()));
            return ranking;
        }));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接任务详情
     *
     * @param task 任务
     * @param logs 反馈日志
     * @param loginUserId 当前登录用户编号
     * @return 任务响应
     */
    private OaTaskRespVO buildTaskRespVO(OaTaskDO task, List<OaTaskLogDO> logs, Long loginUserId) {
        if (task == null) {
            return null;
        }
        return CollUtil.getFirst(buildTaskRespVOList(Collections.singletonList(task), logs, loginUserId));
    }

    /**
     * 拼接任务响应列表
     *
     * @param tasks 任务列表
     * @param logs 任务反馈日志列表
     * @param loginUserId 当前登录用户编号
     * @return 任务响应列表
     */
    private List<OaTaskRespVO> buildTaskRespVOList(List<OaTaskDO> tasks, List<OaTaskLogDO> logs,
                                                 Long loginUserId) {
        if (CollUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        // 1. 查询接收人、发布人和反馈人信息
        Map<Long, List<OaTaskReceiverDO>> receiverListMap =
                taskService.getTaskReceiverListMap(convertSet(tasks, OaTaskDO::getId));
        Set<Long> userIds = Stream.of(
                tasks.stream().map(task -> NumberUtils.parseLong(task.getCreator())),
                receiverListMap.values().stream().flatMap(Collection::stream).map(OaTaskReceiverDO::getUserId),
                logs.stream().map(OaTaskLogDO::getUserId))
                .flatMap(stream -> stream).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 2. 拼接任务、接收人和反馈信息
        Map<Long, List<OaTaskLogDO>> logListMap = convertMultiMap(logs, OaTaskLogDO::getTaskId);
        return convertList(tasks, taskDO -> {
            OaTaskRespVO task = BeanUtils.toBean(taskDO, OaTaskRespVO.class)
                    .setPublisherUserId(NumberUtils.parseLong(taskDO.getCreator()))
                    .setReceivers(Collections.emptyList()).setLogs(Collections.emptyList());
            MapUtils.findAndThen(userMap, task.getPublisherUserId(), user -> {
                task.setPublisherUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> task.setPublisherDeptName(dept.getName()));
            });
            MapUtils.findAndThen(receiverListMap, task.getId(), receivers -> {
                task.setReceivers(BeanUtils.toBean(receivers, OaTaskReceiverRespVO.class, receiver ->
                        MapUtils.findAndThen(userMap, receiver.getUserId(), user -> {
                            receiver.setUserName(user.getNickname());
                            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> receiver.setDeptName(dept.getName()));
                        })));
                OaTaskReceiverDO loginUserReceiver = CollUtil.findOne(receivers, receiver -> receiver.getUserId().equals(loginUserId));
                if (loginUserReceiver != null) {
                    task.setReceiverStatus(loginUserReceiver.getStatus());
                }
            });
            MapUtils.findAndThen(logListMap, task.getId(), taskLogs ->
                    task.setLogs(BeanUtils.toBean(taskLogs, OaTaskLogRespVO.class, log ->
                            MapUtils.findAndThen(userMap, log.getUserId(), user -> log.setUserName(user.getNickname())))));
            return task;
        });
    }

}
