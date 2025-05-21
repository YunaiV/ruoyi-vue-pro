package cn.iocoder.yudao.module.cms.service.comment;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentPageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentRespVO;
import cn.iocoder.yudao.module.cms.convert.comment.CmsCommentConvert;
import cn.iocoder.yudao.module.cms.dal.dataobject.comment.CmsCommentDO;
import cn.iocoder.yudao.module.cms.dal.mysql.comment.CmsCommentMapper;
import cn.iocoder.yudao.module.cms.service.article.CmsArticleService;
// TODO: Define CMS specific error codes
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DEPT_NOT_FOUND; // Placeholder for ARTICLE_NOT_FOUND / COMMENT_NOT_FOUND
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.BAD_REQUEST;    // Placeholder for INVALID_STATUS

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class CmsCommentServiceImpl implements CmsCommentService {

    // Define status constants or use an Enum
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_SPAM = 2;

    @Resource
    private CmsCommentMapper commentMapper;

    @Resource
    private CmsCommentConvert commentConvert;

    @Resource
    private CmsArticleService articleService; // To validate article existence

    // TODO: Inject UserService if needed to enrich user details for CmsCommentRespVO

    @Override
    public Long createComment(CmsCommentCreateReqVO createReqVO) {
        // Validate article exists
        articleService.getArticle(createReqVO.getArticleId()); // Assumes getArticle throws if not found

        CmsCommentDO comment = commentConvert.convert(createReqVO);
        // Default status if not provided, or validate if it's a valid initial status
        if (comment.getStatus() == null) {
            comment.setStatus(STATUS_PENDING);
        }
        // TODO: Set userId if admin is creating on behalf of a logged-in user, or if context implies it.
        // For now, userId is taken directly from VO if provided.
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public void updateCommentStatus(Long id, Integer status) {
        validateCommentExists(id);
        // Validate status value
        if (status == null || (status != STATUS_PENDING && status != STATUS_APPROVED && status != STATUS_SPAM)) {
            throw ServiceExceptionUtil.exception(BAD_REQUEST); // Placeholder for INVALID_COMMENT_STATUS
        }

        CmsCommentDO updateDO = new CmsCommentDO();
        updateDO.setId(id);
        updateDO.setStatus(status);
        commentMapper.updateById(updateDO);
    }

    @Override
    public void deleteComment(Long id) {
        validateCommentExists(id);
        // TODO: Consider if there are child comments to handle (e.g., orphan them or delete them)
        commentMapper.deleteById(id);
    }

    @Override
    public CmsCommentRespVO getComment(Long id) {
        CmsCommentDO comment = commentMapper.selectById(id);
        return enrichCmsCommentRespVO(comment);
    }
    
    @Override
    public CmsCommentDO getCommentDoById(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    public PageResult<CmsCommentRespVO> getCommentAdminPage(CmsCommentPageReqVO pageReqVO) {
        PageResult<CmsCommentDO> pageResult = commentMapper.selectPage(pageReqVO);
        List<CmsCommentRespVO> enrichedList = pageResult.getList().stream()
                .map(this::enrichCmsCommentRespVO)
                .collect(Collectors.toList());
        return new PageResult<>(enrichedList, pageResult.getTotal());
    }

    @Override
    public PageResult<CmsCommentRespVO> getApprovedCommentPageByArticle(Long articleId, PageParam pageParam) {
        PageResult<CmsCommentDO> pageResult = commentMapper.selectPageByArticleIdAndStatus(articleId, STATUS_APPROVED, pageParam);
        List<CmsCommentRespVO> enrichedList = pageResult.getList().stream()
                .map(this::enrichCmsCommentRespVO)
                .collect(Collectors.toList());
        return new PageResult<>(enrichedList, pageResult.getTotal());
    }

    private void validateCommentExists(Long id) {
        if (commentMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND); // Placeholder for COMMENT_NOT_FOUND
        }
    }
    
    private CmsCommentRespVO enrichCmsCommentRespVO(CmsCommentDO comment) {
        if (comment == null) {
            return null;
        }
        CmsCommentRespVO respVO = commentConvert.convert(comment);
        // Enrich with Article Title
        if (respVO.getArticleId() != null) {
            // CmsArticleRespVO article = articleService.getArticle(respVO.getArticleId()); // This returns RespVO
            // For now, let's assume we just need the title and avoid circular service calls if possible,
            // or the articleService.getArticle method is lightweight.
            // If articleService.getArticle is too heavy or causes issues, create a lighter getArticleTitle method.
            // For now, we'll skip direct article title enrichment here to avoid complexity,
            // assuming it can be handled by frontend if articleId is present, or by a more complex query.
            // respVO.setArticleTitle(article != null ? article.getTitle() : null);
        }
        // Enrich with User Nickname (if userId is present)
        if (respVO.getUserId() != null) {
            // TODO: Call UserService to get user details
            // UserDO user = userService.getUser(respVO.getUserId());
            // respVO.setUserNickname(user != null ? user.getNickname() : null);
        }
        return respVO;
    }
}
