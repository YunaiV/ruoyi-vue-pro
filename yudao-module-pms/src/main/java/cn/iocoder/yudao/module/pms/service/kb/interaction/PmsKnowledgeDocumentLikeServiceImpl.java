package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentLikeDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeDocumentLikeMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * PMS 知识文档点赞 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeDocumentLikeServiceImpl implements PmsKnowledgeDocumentLikeService {

    @Resource
    private PmsKnowledgeDocumentLikeMapper documentLikeMapper;
    @Resource
    private PmsKnowledgeInteractionTargetService interactionTargetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDocumentLike(Long documentId, Long userId) {
        // 1. 校验文档可读
        interactionTargetService.validateTargetReadable(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId, userId);

        // 2. 新增点赞关系，重复请求保持幂等
        PmsKnowledgeDocumentLikeDO like = documentLikeMapper.selectByDocumentIdAndUserId(documentId, userId);
        if (like != null) {
            return;
        }
        documentLikeMapper.insert(new PmsKnowledgeDocumentLikeDO().setDocumentId(documentId).setUserId(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocumentLike(Long documentId, Long userId) {
        // 1. 校验文档可读
        interactionTargetService.validateTargetReadable(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId, userId);

        // 2. 删除点赞关系，重复请求保持幂等
        PmsKnowledgeDocumentLikeDO like = documentLikeMapper.selectByDocumentIdAndUserId(documentId, userId);
        if (like != null) {
            documentLikeMapper.deleteById(like.getId());
        }
    }

    @Override
    public List<PmsKnowledgeDocumentLikeDO> getDocumentLikeList(Long documentId) {
        return documentLikeMapper.selectListByDocumentId(documentId);
    }

    @Override
    public void deleteLikesByDocumentIds(Collection<Long> documentIds) {
        if (CollUtil.isEmpty(documentIds)) {
            return;
        }
        documentLikeMapper.deleteByDocumentIds(documentIds);
    }

}
