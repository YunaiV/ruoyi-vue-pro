package cn.iocoder.yudao.module.bpm.service.comment;

import cn.iocoder.yudao.module.bpm.controller.admin.comment.vo.BpmCommentCreateReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import jakarta.validation.Valid;
import org.flowable.engine.task.Comment;

import java.util.List;

/**
 * 流程评论 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmCommentService {

    /**
     * 获得指定流程实例的评论列表
     *
     * @param processInstanceId 流程实例的编号
     * @return 评论列表
     */
    List<Comment> getCommentListByProcessInstanceId(String processInstanceId);

    /**
     * 创建流程评论
     *
     * @param reqVO 评论请求
     */
    void createComment(@Valid BpmCommentCreateReqVO reqVO);

    /**
     * 创建流程评论
     *
     * @param taskId            任务编号
     * @param processInstanceId 流程实例编号
     * @param type              评论类型
     * @param params            评论模板参数
     */
    void createComment(String taskId, String processInstanceId, BpmCommentTypeEnum type, Object... params);

}
