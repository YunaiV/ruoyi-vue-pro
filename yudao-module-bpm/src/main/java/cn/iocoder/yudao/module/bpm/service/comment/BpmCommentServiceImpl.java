package cn.iocoder.yudao.module.bpm.service.comment;

import cn.iocoder.yudao.module.bpm.controller.admin.comment.vo.BpmCommentCreateReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.flowable.engine.TaskService;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程评论 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class BpmCommentServiceImpl implements BpmCommentService {

    @Resource
    private TaskService taskService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmTaskService bpmTaskService;

    @Override
    public List<Comment> getCommentListByProcessInstanceId(String processInstanceId) {
        return taskService.getProcessInstanceComments(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(@Valid BpmCommentCreateReqVO reqVO) {
        Task task = bpmTaskService.validateTaskExists(reqVO.getTaskId());
        createComment(task.getId(), task.getProcessInstanceId(), BpmCommentTypeEnum.COMMENT, reqVO.getMessage());
    }

    @Override
    public void createComment(String taskId, String processInstanceId, BpmCommentTypeEnum type, Object... params) {
        taskService.addComment(taskId, processInstanceId, type.getType(), type.formatComment(params));
    }

}
