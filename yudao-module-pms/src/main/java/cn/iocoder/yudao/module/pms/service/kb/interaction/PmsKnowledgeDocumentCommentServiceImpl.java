package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.comment.PmsKnowledgeDocumentCommentSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentCommentDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeDocumentCommentMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_COMMENT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_COMMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_COMMENT_REPLY_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_COMMENT_TYPE_INVALID;

/**
 * PMS 知识库文档评论 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeDocumentCommentServiceImpl implements PmsKnowledgeDocumentCommentService {

    @Resource
    private PmsKnowledgeDocumentCommentMapper commentMapper;

    @Resource
    @Lazy // 延迟加载，避免与文档回收逻辑形成循环依赖
    private PmsKnowledgeDocumentService documentService;

    @Override
    public Long createDocumentComment(PmsKnowledgeDocumentCommentSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验文档可评论
        validateDocumentCommentable(saveReqVO.getDocumentId(), userId);
        // 1.2 校验回复关系
        Long mainId = saveReqVO.getMainId() != null ? saveReqVO.getMainId()
                : PmsKnowledgeDocumentCommentDO.MAIN_ID_ROOT;
        if (ObjectUtil.notEqual(PmsKnowledgeDocumentCommentDO.MAIN_ID_ROOT, mainId)) {
            PmsKnowledgeDocumentCommentDO mainComment = validateCommentExists(mainId);
            if (ObjectUtil.notEqual(saveReqVO.getDocumentId(), mainComment.getDocumentId())
                    || ObjectUtil.notEqual(PmsKnowledgeDocumentCommentDO.MAIN_ID_ROOT, mainComment.getMainId())) {
                throw exception(KNOWLEDGE_DOCUMENT_COMMENT_REPLY_INVALID);
            }
        }

        // 2. 创建评论
        PmsKnowledgeDocumentCommentDO comment = BeanUtils.toBean(saveReqVO, PmsKnowledgeDocumentCommentDO.class)
                .setUserId(userId).setMainId(mainId);
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocumentComment(Long id, Long userId) {
        // 1.1 校验评论存在
        PmsKnowledgeDocumentCommentDO comment = validateCommentExists(id);
        // 1.2 校验文档可评论
        validateDocumentCommentable(comment.getDocumentId(), userId);
        // 1.3 校验评论属于当前用户
        validateCommentOwner(comment, userId);

        // 2. 删除主评论时同步删除全部回复
        if (PmsKnowledgeDocumentCommentDO.MAIN_ID_ROOT.equals(comment.getMainId())) {
            commentMapper.deleteByMainId(comment.getId());
        }
        commentMapper.deleteById(comment.getId());
    }

    @Override
    public List<PmsKnowledgeDocumentCommentDO> getDocumentCommentList(Long documentId, Long userId) {
        // 1. 校验文档可访问
        validateDocumentCommentable(documentId, userId);

        // 2. 查询评论和回复
        return commentMapper.selectListByDocumentId(documentId);
    }

    @Override
    public void deleteCommentsByDocumentIds(Collection<Long> documentIds) {
        if (CollUtil.isEmpty(documentIds)) {
            return;
        }
        commentMapper.deleteByDocumentIds(documentIds);
    }

    private PmsKnowledgeDocumentCommentDO validateCommentExists(Long id) {
        PmsKnowledgeDocumentCommentDO comment = commentMapper.selectById(id);
        if (comment == null) {
            throw exception(KNOWLEDGE_DOCUMENT_COMMENT_NOT_EXISTS);
        }
        return comment;
    }

    private void validateCommentOwner(PmsKnowledgeDocumentCommentDO comment, Long userId) {
        if (ObjectUtil.notEqual(userId, comment.getUserId())) {
            throw exception(KNOWLEDGE_DOCUMENT_COMMENT_ACCESS_DENIED);
        }
    }

    private void validateDocumentCommentable(Long documentId, Long userId) {
        PmsKnowledgeDocumentDO document = documentService.getDocument(documentId, userId);
        if (!PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType().equals(document.getType())) {
            throw exception(KNOWLEDGE_DOCUMENT_COMMENT_TYPE_INVALID);
        }
    }

}
