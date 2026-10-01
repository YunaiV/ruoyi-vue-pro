package cn.iocoder.yudao.module.ai1.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBasePageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBaseSaveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeSearchRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.knowledge.Ai1KnowledgeBaseMapper;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import cn.iocoder.yudao.module.ai1.harness.rag.Ai1RagTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;

/**
 * AI1 知识库 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class Ai1KnowledgeBaseServiceImpl implements Ai1KnowledgeBaseService {

    @Resource
    private Ai1KnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1KnowledgeDocumentService knowledgeDocumentService;
    @Resource
    private Ai1ModelService modelService;

    @Resource
    private Ai1RagTool ragTool;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1AgentService agentService;

    @Override
    public Long createKnowledgeBase(Ai1KnowledgeBaseSaveReqVO createReqVO) {
        // 1. 校验向量化模型
        validateEmbeddingModel(createReqVO.getEmbeddingProviderId(), createReqVO.getEmbeddingModelId());

        // 2. 插入
        Ai1KnowledgeBaseDO knowledgeBase = BeanUtils.toBean(createReqVO, Ai1KnowledgeBaseDO.class);
        knowledgeBaseMapper.insert(knowledgeBase);
        return knowledgeBase.getId();
    }

    @Override
    public void updateKnowledgeBase(Ai1KnowledgeBaseSaveReqVO updateReqVO) {
        // 1. 校验存在、向量化模型
        Ai1KnowledgeBaseDO knowledgeBase = validateKnowledgeBaseExists(updateReqVO.getId());
        validateEmbeddingModel(updateReqVO.getEmbeddingProviderId(), updateReqVO.getEmbeddingModelId());

        // 2. 更新
        Ai1KnowledgeBaseDO updateObj = BeanUtils.toBean(updateReqVO, Ai1KnowledgeBaseDO.class);
        knowledgeBaseMapper.updateById(updateObj);

        // 3. 向量化模型变更时，失效向量存储缓存，保证后续按新模型重建
        if (ObjUtil.notEqual(knowledgeBase.getEmbeddingProviderId(), updateObj.getEmbeddingProviderId())
                || ObjUtil.notEqual(knowledgeBase.getEmbeddingModelId(), updateObj.getEmbeddingModelId())) {
            ragTool.evict(updateObj.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(Long id) {
        deleteKnowledgeBaseListByIds(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBaseListByIds(List<Long> ids) {
        // 1.1 校验存在
        ids.forEach(this::validateKnowledgeBaseExists);
        // 1.2 校验未被 Agent 绑定
        if (agentService.getAgentCountByKnowledgeBaseIds(ids) > 0) {
            throw exception(KNOWLEDGE_BASE_USED_BY_AGENT);
        }

        // 2. 删除知识库与其下文档
        knowledgeBaseMapper.deleteByIds(ids);
        knowledgeDocumentService.deleteKnowledgeDocumentListByKnowledgeBaseIds(ids);

        // 3. 删除 Milvus 向量集合：外部资源无法回滚，事务提交后再执行，失败仅记录日志
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ids.forEach(ragTool::dropCollection);
            }
        });
    }

    @Override
    public Ai1KnowledgeBaseDO getKnowledgeBase(Long id) {
        return knowledgeBaseMapper.selectById(id);
    }

    @Override
    public Ai1KnowledgeBaseDO validateKnowledgeBaseExists(Long id) {
        Ai1KnowledgeBaseDO knowledgeBase = knowledgeBaseMapper.selectById(id);
        if (knowledgeBase == null) {
            throw exception(KNOWLEDGE_BASE_NOT_EXISTS);
        }
        return knowledgeBase;
    }

    @Override
    public PageResult<Ai1KnowledgeBaseDO> getKnowledgeBasePage(Ai1KnowledgeBasePageReqVO pageReqVO) {
        return knowledgeBaseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Ai1KnowledgeBaseDO> getKnowledgeBaseListByStatus(Integer status) {
        return knowledgeBaseMapper.selectListByStatus(status);
    }

    @Override
    public List<Ai1KnowledgeBaseDO> getKnowledgeBaseList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return knowledgeBaseMapper.selectByIds(ids);
    }

    @Override
    public Long getKnowledgeBaseCountByEmbeddingModelIds(Collection<Long> modelIds) {
        if (CollUtil.isEmpty(modelIds)) {
            return 0L;
        }
        return knowledgeBaseMapper.selectCountByEmbeddingModelIds(modelIds);
    }

    @Override
    public List<Ai1KnowledgeSearchRespVO> searchKnowledgeBase(Long id, String query, Integer topK) {
        // 1. 校验存在（当前租户下），再访问其向量集合
        Ai1KnowledgeBaseDO knowledgeBase = validateKnowledgeBaseExists(id);

        // 2. 检索：业务异常（如模型被禁用）直接抛出，其余异常包装为检索失败
        try {
            return ragTool.search(knowledgeBase, query, topK);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[searchKnowledgeBase][知识库({}) query({}) topK({}) 向量检索失败]", id, query, topK, e);
            throw exception(KNOWLEDGE_BASE_SEARCH_FAIL, e.getMessage());
        }
    }

    /**
     * 校验向量化模型：Provider、模型存在且开启，模型归属于该 Provider，且为嵌入模型
     */
    private void validateEmbeddingModel(Long providerId, Long modelId) {
        Ai1ModelRespBO model = modelService.getModelRespBO(providerId, modelId);
        if (!Ai1ModelTypeEnum.isEmbedding(model.getModelType())) {
            throw exception(MODEL_TYPE_NOT_EMBEDDING);
        }
    }

}
