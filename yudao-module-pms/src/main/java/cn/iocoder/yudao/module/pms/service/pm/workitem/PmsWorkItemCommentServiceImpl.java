package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.comment.PmsWorkItemCommentSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemCommentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemCommentMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_COMMENT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_COMMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_COMMENT_REPLY_INVALID;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.COMMENT_CREATED;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.COMMENT_DELETED;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.COMMENT_REPLIED;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.COMMENT_UPDATED;

/**
 * PMS 工作项评论 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkItemCommentServiceImpl implements PmsWorkItemCommentService {

    @Resource
    private PmsWorkItemCommentMapper commentMapper;

    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private PmsWorkItemActivityService workItemActivityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkItemComment(PmsWorkItemCommentSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验工作项可编辑
        PmsWorkItemDO workItem = workItemService.getWritableWorkItem(saveReqVO.getWorkItemId(), userId);
        // 1.2 校验回复关系
        Long mainId = saveReqVO.getMainId() != null ? saveReqVO.getMainId() : PmsWorkItemCommentDO.MAIN_ID_ROOT;
        if (ObjectUtil.notEqual(PmsWorkItemCommentDO.MAIN_ID_ROOT, mainId)) {
            PmsWorkItemCommentDO mainComment = validateCommentExists(mainId);
            if (ObjectUtil.notEqual(saveReqVO.getWorkItemId(), mainComment.getWorkItemId())
                    || ObjectUtil.notEqual(PmsWorkItemCommentDO.MAIN_ID_ROOT, mainComment.getMainId())) {
                throw exception(WORK_ITEM_COMMENT_REPLY_INVALID);
            }
        }

        // 2.1 创建评论
        PmsWorkItemCommentDO comment = BeanUtils.toBean(saveReqVO, PmsWorkItemCommentDO.class)
                .setUserId(userId).setMainId(mainId);
        commentMapper.insert(comment);
        // 2.2 记录评论动态
        workItemActivityService.createWorkItemActivity(workItem.getProjectId(), workItem.getId(), userId,
                ObjectUtil.equal(PmsWorkItemCommentDO.MAIN_ID_ROOT, mainId) ? COMMENT_CREATED : COMMENT_REPLIED);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemComment(PmsWorkItemCommentSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验评论存在
        PmsWorkItemCommentDO comment = validateCommentExists(saveReqVO.getId());
        // 1.2 校验工作项可编辑
        PmsWorkItemDO workItem = workItemService.getWritableWorkItem(comment.getWorkItemId(), userId);
        // 1.3 校验评论属于当前用户
        validateCommentOwner(comment, userId);

        // 2.1 更新评论内容
        commentMapper.updateById(BeanUtils.toBean(saveReqVO, PmsWorkItemCommentDO.class).setId(comment.getId()));
        // 2.2 记录评论更新动态
        workItemActivityService.createWorkItemActivity(
                workItem.getProjectId(), workItem.getId(), userId, COMMENT_UPDATED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkItemComment(Long id, Long userId) {
        // 1.1 校验评论存在
        PmsWorkItemCommentDO comment = validateCommentExists(id);
        // 1.2 校验工作项可编辑
        PmsWorkItemDO workItem = workItemService.getWritableWorkItem(comment.getWorkItemId(), userId);
        // 1.3 校验评论属于当前用户
        validateCommentOwner(comment, userId);

        // 2.1 删除主评论时同步删除全部回复
        if (PmsWorkItemCommentDO.MAIN_ID_ROOT.equals(comment.getMainId())) {
            commentMapper.deleteByMainId(comment.getId());
        }
        commentMapper.deleteById(comment.getId());
        // 2.2 记录评论删除动态
        workItemActivityService.createWorkItemActivity(
                workItem.getProjectId(), workItem.getId(), userId, COMMENT_DELETED);
    }

    @Override
    public List<PmsWorkItemCommentDO> getWorkItemCommentList(Long workItemId, Long userId) {
        // 1. 校验工作项可访问
        workItemService.getWorkItem(workItemId, userId);

        // 2. 查询评论和回复
        return commentMapper.selectListByWorkItemId(workItemId);
    }

    /**
     * 校验工作项评论存在
     *
     * @param id 评论编号
     * @return 工作项评论
     */
    private PmsWorkItemCommentDO validateCommentExists(Long id) {
        PmsWorkItemCommentDO comment = commentMapper.selectById(id);
        if (comment == null) {
            throw exception(WORK_ITEM_COMMENT_NOT_EXISTS);
        }
        return comment;
    }

    /**
     * 校验当前用户是评论创建人
     *
     * @param comment 工作项评论
     * @param userId 后台用户编号
     */
    private void validateCommentOwner(PmsWorkItemCommentDO comment, Long userId) {
        if (ObjectUtil.notEqual(userId, comment.getUserId())) {
            throw exception(WORK_ITEM_COMMENT_ACCESS_DENIED);
        }
    }

    @Override
    public void deleteWorkItemCommentListByWorkItemId(Long workItemId) {
        commentMapper.deleteByWorkItemId(workItemId);
    }

    @Override
    public void deleteWorkItemCommentListByWorkItemIds(Collection<Long> workItemIds) {
        if (CollUtil.isEmpty(workItemIds)) {
            return;
        }
        commentMapper.deleteByWorkItemIds(workItemIds);
    }
}
