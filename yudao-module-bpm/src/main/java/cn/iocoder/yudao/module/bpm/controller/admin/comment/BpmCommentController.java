package cn.iocoder.yudao.module.bpm.controller.admin.comment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import cn.iocoder.yudao.module.bpm.controller.admin.comment.vo.BpmCommentCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.comment.vo.BpmCommentRespVO;
import cn.iocoder.yudao.module.bpm.service.comment.BpmCommentService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 流程评论")
@RestController
@RequestMapping("/bpm/comment")
@Validated
public class BpmCommentController {

    @Resource
    private BpmCommentService commentService;
    @Resource
    private BpmTaskService taskService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/list-by-process-instance-id")
    @Operation(summary = "获得指定流程实例的评论列表")
    @Parameter(name = "processInstanceId", description = "流程实例的编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<List<BpmCommentRespVO>> getCommentListByProcessInstanceId(
            @RequestParam("processInstanceId") String processInstanceId) {
        // 获得评论列表
        List<Comment> commentList = commentService.getCommentListByProcessInstanceId(processInstanceId);
        if (CollUtil.isEmpty(commentList)) {
            return success(Collections.emptyList());
        }

        // 拼接 VO
        Map<String, HistoricTaskInstance> taskMap = taskService.getHistoricTaskMap(
                convertSet(commentList, Comment::getTaskId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(commentList, comment -> NumberUtils.parseLong(comment.getUserId())));
        return success(convertList(commentList, comment -> BeanUtils.toBean(comment, BpmCommentRespVO.class, commentVO -> {
            commentVO.setMessage(comment.getFullMessage())
                    .setCreateTime(DateUtils.of(comment.getTime()))
                    .setTask(BeanUtils.toBean(taskMap.get(comment.getTaskId()), BpmCommentRespVO.Task.class))
                    .setUser(BeanUtils.toBean(userMap.get(NumberUtils.parseLong(comment.getUserId())), UserSimpleBaseVO.class));
        })));
    }

    @PostMapping("/create")
    @Operation(summary = "创建流程评论")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> createComment(@Valid @RequestBody BpmCommentCreateReqVO reqVO) {
        commentService.createComment(reqVO);
        return success(true);
    }

}
