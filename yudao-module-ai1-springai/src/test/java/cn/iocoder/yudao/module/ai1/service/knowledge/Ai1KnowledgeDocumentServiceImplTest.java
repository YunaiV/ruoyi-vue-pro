package cn.iocoder.yudao.module.ai1.service.knowledge;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.document.Ai1KnowledgeDocumentSaveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.document.Ai1KnowledgeDocumentVectorizeRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.knowledge.Ai1KnowledgeDocumentMapper;
import cn.iocoder.yudao.module.ai1.enums.knowledge.Ai1KnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.ai1.tool.rag.Ai1RagTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1KnowledgeDocumentServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1KnowledgeDocumentServiceImpl.class)
public class Ai1KnowledgeDocumentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1KnowledgeDocumentServiceImpl knowledgeDocumentService;

    @Resource
    private Ai1KnowledgeDocumentMapper knowledgeDocumentMapper;

    @MockitoBean
    private Ai1KnowledgeBaseService knowledgeBaseService;
    @MockitoBean
    private Ai1RagTool ragTool;

    @Test
    public void testCreateKnowledgeDocument_initUnprocessed() {
        // 调用
        Long id = knowledgeDocumentService.createKnowledgeDocument(new Ai1KnowledgeDocumentSaveReqVO()
                .setKnowledgeBaseId(1L).setName("三体.txt").setContent("三体舰队"));

        // 断言：初始为未处理
        Ai1KnowledgeDocumentDO document = knowledgeDocumentMapper.selectById(id);
        assertEquals(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus(), document.getStatus());
        assertEquals(0, document.getChunkCount());
        verify(knowledgeBaseService).validateKnowledgeBaseExists(1L);
    }

    @Test
    public void testUploadKnowledgeDocument_fileTypeInvalid() {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", null, "x".getBytes(StandardCharsets.UTF_8));
        assertServiceException(() -> knowledgeDocumentService.uploadKnowledgeDocument(1L, file), KNOWLEDGE_DOCUMENT_FILE_TYPE_INVALID);
    }

    @Test
    public void testUploadKnowledgeDocument_success() {
        // 准备参数
        MockMultipartFile file = new MockMultipartFile("file", "说明.MD", null, "# 标题".getBytes(StandardCharsets.UTF_8));

        // 调用
        Long id = knowledgeDocumentService.uploadKnowledgeDocument(1L, file);

        // 断言
        Ai1KnowledgeDocumentDO document = knowledgeDocumentMapper.selectById(id);
        assertEquals("说明.MD", document.getName());
        assertEquals("# 标题", document.getContent());
    }

    @Test
    public void testUpdateKnowledgeDocument_contentChanged() {
        // mock 数据：已向量化的文档
        Ai1KnowledgeDocumentDO dbDocument = insertDocument(Ai1KnowledgeDocumentStatusEnum.VECTORIZED.getStatus(), "旧内容");
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(dbDocument.getKnowledgeBaseId());
        when(knowledgeBaseService.getKnowledgeBase(dbDocument.getKnowledgeBaseId())).thenReturn(knowledgeBase);

        // 调用：修改内容
        knowledgeDocumentService.updateKnowledgeDocument(new Ai1KnowledgeDocumentSaveReqVO().setId(dbDocument.getId())
                .setKnowledgeBaseId(999L).setName("新名称").setContent("新内容"));

        // 断言：重置为未处理并清理旧向量；归属知识库不变
        Ai1KnowledgeDocumentDO document = knowledgeDocumentMapper.selectById(dbDocument.getId());
        assertEquals(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus(), document.getStatus());
        assertEquals(0, document.getChunkCount());
        assertEquals(dbDocument.getKnowledgeBaseId(), document.getKnowledgeBaseId());
        verify(ragTool).deleteByDocumentIds(knowledgeBase, Collections.singletonList(dbDocument.getId()));
    }

    @Test
    public void testUpdateKnowledgeDocument_nameOnly() {
        // mock 数据
        Ai1KnowledgeDocumentDO dbDocument = insertDocument(Ai1KnowledgeDocumentStatusEnum.VECTORIZED.getStatus(), "内容");

        // 调用：只改名称
        knowledgeDocumentService.updateKnowledgeDocument(new Ai1KnowledgeDocumentSaveReqVO().setId(dbDocument.getId())
                .setKnowledgeBaseId(dbDocument.getKnowledgeBaseId()).setName("新名称").setContent("内容"));

        // 断言：向量化状态保持，不清理向量
        assertEquals(Ai1KnowledgeDocumentStatusEnum.VECTORIZED.getStatus(),
                knowledgeDocumentMapper.selectById(dbDocument.getId()).getStatus());
        verifyNoInteractions(ragTool);
    }

    @Test
    public void testVectorizeKnowledgeDocument_success() {
        // mock 数据
        Ai1KnowledgeDocumentDO dbDocument = insertDocument(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus(), "内容");
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(dbDocument.getKnowledgeBaseId());
        when(knowledgeBaseService.validateKnowledgeBaseExists(dbDocument.getKnowledgeBaseId())).thenReturn(knowledgeBase);
        when(ragTool.vectorize(knowledgeBase, dbDocument.getId(), "内容")).thenReturn(3);

        // 调用
        Integer chunkCount = knowledgeDocumentService.vectorizeKnowledgeDocument(dbDocument.getId());

        // 断言
        assertEquals(3, chunkCount);
        Ai1KnowledgeDocumentDO document = knowledgeDocumentMapper.selectById(dbDocument.getId());
        assertEquals(Ai1KnowledgeDocumentStatusEnum.VECTORIZED.getStatus(), document.getStatus());
        assertEquals(3, document.getChunkCount());
    }

    @Test
    public void testVectorizeKnowledgeDocument_fail() {
        // mock 数据：Milvus 不可用
        Ai1KnowledgeDocumentDO dbDocument = insertDocument(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus(), "内容");
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(dbDocument.getKnowledgeBaseId());
        when(knowledgeBaseService.validateKnowledgeBaseExists(dbDocument.getKnowledgeBaseId())).thenReturn(knowledgeBase);
        when(ragTool.vectorize(any(), anyLong(), anyString())).thenThrow(new IllegalStateException("连接失败"));

        // 调用，并断言异常
        assertServiceException(() -> knowledgeDocumentService.vectorizeKnowledgeDocument(dbDocument.getId()),
                KNOWLEDGE_DOCUMENT_VECTORIZE_FAIL, "连接失败");

        // 断言：回写失败状态
        assertEquals(Ai1KnowledgeDocumentStatusEnum.FAILED.getStatus(),
                knowledgeDocumentMapper.selectById(dbDocument.getId()).getStatus());
    }

    @Test
    public void testVectorizeListByKnowledgeBaseId_partialFail() {
        // mock 数据：3 个文档，其中 1 个空内容跳过、1 个失败
        Ai1KnowledgeDocumentDO success = insertDocument(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus(), "成功");
        Ai1KnowledgeDocumentDO failure = insertDocument(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus(), "失败");
        insertDocument(Ai1KnowledgeDocumentStatusEnum.UNPROCESSED.getStatus(), "");
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(success.getKnowledgeBaseId());
        when(knowledgeBaseService.validateKnowledgeBaseExists(knowledgeBase.getId())).thenReturn(knowledgeBase);
        when(ragTool.vectorize(knowledgeBase, success.getId(), "成功")).thenReturn(1);
        when(ragTool.vectorize(knowledgeBase, failure.getId(), "失败")).thenThrow(new IllegalStateException("嵌入失败"));

        // 调用
        Ai1KnowledgeDocumentVectorizeRespVO result = knowledgeDocumentService
                .vectorizeKnowledgeDocumentListByKnowledgeBaseId(knowledgeBase.getId());

        // 断言
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    public void testDeleteKnowledgeDocumentList_cleanVectorsByKnowledgeBase() {
        // mock 数据
        Ai1KnowledgeDocumentDO document1 = insertDocument(Ai1KnowledgeDocumentStatusEnum.VECTORIZED.getStatus(), "1");
        Ai1KnowledgeDocumentDO document2 = insertDocument(Ai1KnowledgeDocumentStatusEnum.VECTORIZED.getStatus(), "2");
        Ai1KnowledgeBaseDO knowledgeBase = new Ai1KnowledgeBaseDO().setId(document1.getKnowledgeBaseId());
        when(knowledgeBaseService.getKnowledgeBaseList(anyCollection())).thenReturn(Collections.singletonList(knowledgeBase));

        // 调用
        knowledgeDocumentService.deleteKnowledgeDocumentListByIds(Arrays.asList(document1.getId(), document2.getId()));

        // 断言
        assertNull(knowledgeDocumentMapper.selectById(document1.getId()));
        assertNull(knowledgeDocumentMapper.selectById(document2.getId()));
        verify(ragTool).deleteByDocumentIds(knowledgeBase, Arrays.asList(document1.getId(), document2.getId()));
    }

    // ========== 随机对象 ==========

    private Ai1KnowledgeDocumentDO insertDocument(Integer status, String content) {
        Ai1KnowledgeDocumentDO document = new Ai1KnowledgeDocumentDO().setKnowledgeBaseId(1L).setName("三体.txt")
                .setContent(content).setChunkCount(0).setStatus(status);
        knowledgeDocumentMapper.insert(document);
        return document;
    }

}
