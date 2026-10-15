package cn.iocoder.yudao.module.ai1.service.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBaseSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.knowledge.Ai1KnowledgeBaseMapper;
import cn.iocoder.yudao.module.ai1.harness.rag.Ai1RagTool;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Ai1KnowledgeBaseServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1KnowledgeBaseServiceImpl.class)
public class Ai1KnowledgeBaseServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1KnowledgeBaseServiceImpl knowledgeBaseService;
    @Resource
    private Ai1KnowledgeBaseMapper knowledgeBaseMapper;

    @MockitoBean
    private Ai1KnowledgeDocumentService knowledgeDocumentService;
    @MockitoBean
    private Ai1ModelService modelService;
    @MockitoBean
    private Ai1RagTool ragTool;
    @MockitoBean
    private Ai1AgentService agentService;

    @Test
    public void testGetKnowledgeBaseCountByEmbeddingModelIds() {
        // mock 数据：开启与关闭的知识库都算引用，已逻辑删除和其他模型的不算
        knowledgeBaseMapper.insert(new Ai1KnowledgeBaseDO().setName("开启的知识库").setEmbeddingModelId(10L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        knowledgeBaseMapper.insert(new Ai1KnowledgeBaseDO().setName("关闭的知识库").setEmbeddingModelId(20L)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        knowledgeBaseMapper.insert(new Ai1KnowledgeBaseDO().setName("其他模型").setEmbeddingModelId(30L));
        Ai1KnowledgeBaseDO deleted = new Ai1KnowledgeBaseDO().setName("已删除").setEmbeddingModelId(10L);
        knowledgeBaseMapper.insert(deleted);
        knowledgeBaseMapper.deleteById(deleted.getId());

        // 调用，并断言引用数量
        assertEquals(2L, knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(Arrays.asList(10L, 20L)));
        assertEquals(1L, knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(Collections.singletonList(10L)));
    }

    @Test
    public void testGetKnowledgeBaseCountByEmbeddingModelIds_empty() {
        // 调用，并断言空参数及未引用模型
        assertEquals(0L, knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(null));
        assertEquals(0L, knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(Collections.emptyList()));
        assertEquals(0L, knowledgeBaseService.getKnowledgeBaseCountByEmbeddingModelIds(Collections.singletonList(999L)));
    }

    @ParameterizedTest
    @CsvSource({"99,0,false", "100,0,true", "100,99,true", "100,100,false", "500,501,false", "500,-1,false"})
    public void testSaveReqVO_chunkValidation(int chunkSize, int chunkOverlap, boolean valid) {
        // 准备参数
        Ai1KnowledgeBaseSaveReqVO reqVO = new Ai1KnowledgeBaseSaveReqVO().setName("知识库")
                .setEmbeddingProviderId(1L).setEmbeddingModelId(2L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setChunkSize(chunkSize).setChunkOverlap(chunkOverlap).setTopK(5);

        // 调用，并断言边界与组合校验
        if (valid) {
            assertDoesNotThrow(() -> ValidationUtils.validate(reqVO));
        } else {
            assertThrows(ConstraintViolationException.class, () -> ValidationUtils.validate(reqVO));
        }
    }

    @Test
    public void testSaveReqVO_chunkRequired() {
        // 准备参数，缺少分片大小和重叠
        Ai1KnowledgeBaseSaveReqVO reqVO = new Ai1KnowledgeBaseSaveReqVO().setName("知识库")
                .setEmbeddingProviderId(1L).setEmbeddingModelId(2L).setStatus(CommonStatusEnum.ENABLE.getStatus()).setTopK(5);

        // 调用，并断言必填错误而非组合校验空指针
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> ValidationUtils.validate(reqVO));
        assertTrue(ex.getConstraintViolations().stream().anyMatch(v -> "chunkSize".equals(v.getPropertyPath().toString())));
        assertTrue(ex.getConstraintViolations().stream().anyMatch(v -> "chunkOverlap".equals(v.getPropertyPath().toString())));
    }

}
