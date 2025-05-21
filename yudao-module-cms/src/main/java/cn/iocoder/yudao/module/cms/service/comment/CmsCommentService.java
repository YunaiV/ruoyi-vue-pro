package cn.iocoder.yudao.module.cms.service.comment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentPageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentRespVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.comment.CmsCommentDO;

import javax.validation.Valid;

public interface CmsCommentService {

    /**
     * Creates a comment (typically by an admin or for seeding).
     *
     * @param createReqVO The VO with comment data.
     * @return The ID of the created comment.
     */
    Long createComment(@Valid CmsCommentCreateReqVO createReqVO);

    /**
     * Updates the status of a comment.
     *
     * @param id     The ID of the comment to update.
     * @param status The new status for the comment.
     */
    void updateCommentStatus(Long id, Integer status);
    
    /**
     * Updates a comment's content (if admin is allowed to edit).
     * This is an example; the exact fields updatable by admin might vary.
     * For now, let's assume only status is updatable via a dedicated method.
     * If content update is needed, create CmsCommentUpdateReqVO and a method like:
     * void updateComment(CmsCommentUpdateReqVO updateReqVO);
     */

    /**
     * Deletes a comment.
     *
     * @param id The ID of the comment to delete.
     */
    void deleteComment(Long id);

    /**
     * Gets a specific comment by its ID.
     *
     * @param id The ID of the comment.
     * @return The comment response VO, or null if not found.
     */
    CmsCommentRespVO getComment(Long id);

    /**
     * Gets a paginated list of comments for the admin UI.
     *
     * @param pageReqVO Pagination and filtering parameters.
     * @return A paginated list of comment response VOs.
     */
    PageResult<CmsCommentRespVO> getCommentAdminPage(CmsCommentPageReqVO pageReqVO);

    /**
     * Gets a paginated list of approved comments for a specific article (for frontend display).
     *
     * @param articleId The ID of the article.
     * @param pageParam Pagination parameters.
     * @return A paginated list of comment response VOs.
     */
    PageResult<CmsCommentRespVO> getApprovedCommentPageByArticle(Long articleId, PageParam pageParam);

    /**
     * Gets a comment DO by its ID. Internal use.
     *
     * @param id The ID of the comment.
     * @return The comment DO, or null if not found.
     */
    CmsCommentDO getCommentDoById(Long id);
}
