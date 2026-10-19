package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.comment.PmsWorkItemCommentSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemCommentDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 工作项评论 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemCommentService {

    /**
     * 创建工作项评论或回复
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 评论编号
     */
    Long createWorkItemComment(PmsWorkItemCommentSaveReqVO saveReqVO, Long userId);

    /**
     * 更新自己的工作项评论内容
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void updateWorkItemComment(PmsWorkItemCommentSaveReqVO saveReqVO, Long userId);

    /**
     * 删除自己的工作项评论，删除主评论时同步删除全部回复
     *
     * @param id 评论编号
     * @param userId 用户编号
     */
    void deleteWorkItemComment(Long id, Long userId);

    /**
     * 删除工作项的全部评论，用于工作项删除时级联清理
     *
     * @param workItemId 工作项编号
     */
    void deleteWorkItemCommentListByWorkItemId(Long workItemId);

    /**
     * 批量删除工作项的评论，用于项目删除时级联清理
     *
     * @param workItemIds 工作项编号列表
     */
    void deleteWorkItemCommentListByWorkItemIds(Collection<Long> workItemIds);

    /**
     * 获得工作项的评论和回复列表
     *
     * @param workItemId 工作项编号
     * @param userId 用户编号
     * @return 评论列表
     */
    List<PmsWorkItemCommentDO> getWorkItemCommentList(Long workItemId, Long userId);

}
