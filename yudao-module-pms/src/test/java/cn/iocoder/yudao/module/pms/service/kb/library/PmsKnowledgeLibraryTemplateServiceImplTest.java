package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplateSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryTemplateDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryTemplateMapper;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_TEMPLATE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_TEMPLATE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_LIBRARY_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeLibraryTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeLibraryTemplateServiceImpl.class)
public class PmsKnowledgeLibraryTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeLibraryTemplateServiceImpl templateService;

    @Resource
    private PmsKnowledgeLibraryTemplateMapper templateMapper;

    @MockitoBean
    private PmsKnowledgeDocumentService documentService;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        if (templateMapper.selectCount(null) > 0) {
            return;
        }
        for (int i = 1; i <= 8; i++) {
            List<PmsKnowledgeLibraryTemplateDO.Document> documents = new ArrayList<>();
            int documentCount = i == 1 ? 5 : 1;
            for (int j = 1; j <= documentCount; j++) {
                documents.add(new PmsKnowledgeLibraryTemplateDO.Document()
                        .setTitle(i == 1 && j == 1 ? "产品需求文档"
                                : i == 7 && j == 1 ? "2020.01.01 更新日志" : "模板文档" + j)
                        .setContent(i == 1 && j == 1 ? "<h1>基本信息</h1>"
                                : i == 7 && j == 1 ? "<p>2020.01.01 更新日志</p>" : "<p>模板内容</p>"));
            }
            templateMapper.insert(new PmsKnowledgeLibraryTemplateDO().setId((long) i)
                    .setName(i == 1 ? "产品研发" : "模板" + i).setDescription("模板简介")
                    .setStatus(0).setSort(i * 10).setDocuments(documents));
        }
    }

    @Test
    public void testGetTemplateList_success() {
        // 调用
        List<PmsKnowledgeLibraryTemplateDO> templates = templateService
                .getLibraryTemplateList(CommonStatusEnum.ENABLE.getStatus());

        // 断言
        PmsKnowledgeLibraryTemplateDO template = CollUtil.getFirst(templates);
        assertEquals("产品研发", template.getName());
        assertEquals(5, template.getDocuments().size());
        PmsKnowledgeLibraryTemplateDO.Document document = CollUtil.getFirst(template.getDocuments());
        assertEquals("产品需求文档", document.getTitle());
        assertTrue(document.getContent().contains("基本信息"));
    }

    @Test
    public void testGetTemplateList_withStatus() {
        // 准备数据
        templateMapper.insert(new PmsKnowledgeLibraryTemplateDO().setId(99L).setName("关闭模板")
                .setStatus(CommonStatusEnum.DISABLE.getStatus()).setSort(99)
                .setDocuments(Collections.singletonList(new PmsKnowledgeLibraryTemplateDO.Document()
                        .setTitle("关闭文档").setContent("<p>关闭内容</p>"))));
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), templateMapper.selectById(99L).getStatus());

        // 调用，并断言状态过滤
        List<PmsKnowledgeLibraryTemplateDO> enabledTemplates = templateService
                .getLibraryTemplateList(CommonStatusEnum.ENABLE.getStatus());
        List<PmsKnowledgeLibraryTemplateDO> allTemplates = templateService.getLibraryTemplateList(null);
        assertTrue(enabledTemplates.stream().noneMatch(template -> template.getId().equals(99L)));
        assertTrue(allTemplates.stream().anyMatch(template -> template.getId().equals(99L)));
    }

    @Test
    public void testCreateLibraryTemplate_success() {
        // 准备参数
        PmsKnowledgeLibraryTemplateSaveReqVO saveReqVO = new PmsKnowledgeLibraryTemplateSaveReqVO()
                .setName("新模板").setDescription("模板简介").setStatus(0).setSort(100)
                .setDocuments(Collections.singletonList(new PmsKnowledgeLibraryTemplateSaveReqVO.Document()
                        .setTitle("模板文档").setContent("<p>模板内容</p>")));

        // 调用
        Long templateId = templateService.createLibraryTemplate(saveReqVO);

        // 断言
        PmsKnowledgeLibraryTemplateDO template = templateService.getLibraryTemplate(templateId);
        assertEquals("新模板", template.getName());
        assertEquals("模板文档", CollUtil.getFirst(template.getDocuments()).getTitle());
    }

    @Test
    public void testCreateLibraryTemplate_duplicateName() {
        // 准备参数
        PmsKnowledgeLibraryTemplateSaveReqVO saveReqVO = new PmsKnowledgeLibraryTemplateSaveReqVO()
                .setName("产品研发").setStatus(0).setSort(100)
                .setDocuments(Collections.singletonList(new PmsKnowledgeLibraryTemplateSaveReqVO.Document()
                        .setTitle("模板文档").setContent("<p>模板内容</p>")));

        // 调用，并断言异常
        assertServiceException(() -> templateService.createLibraryTemplate(saveReqVO),
                KNOWLEDGE_LIBRARY_TEMPLATE_NAME_DUPLICATE);
    }

    @Test
    public void testCreateLibraryTemplate_invalidDocuments() {
        // 准备参数
        PmsKnowledgeLibraryTemplateSaveReqVO saveReqVO = new PmsKnowledgeLibraryTemplateSaveReqVO()
                .setName("无效模板").setStatus(0).setSort(100).setDocuments(Arrays.asList(
                        new PmsKnowledgeLibraryTemplateSaveReqVO.Document().setTitle("重复").setContent("内容"),
                        new PmsKnowledgeLibraryTemplateSaveReqVO.Document().setTitle("重复").setContent("内容")));

        // 调用，并断言异常
        assertServiceException(() -> templateService.createLibraryTemplate(saveReqVO),
                KNOWLEDGE_LIBRARY_TEMPLATE_INVALID);
    }

    @Test
    public void testCreateTemplateDocumentList_success() {
        // 准备参数
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        Long documentId = randomLongId();
        when(documentService.createDocument(any(), eq(userId))).thenReturn(documentId);

        // 调用
        templateService.createTemplateDocumentList(7L, libraryId, userId);

        // 断言
        ArgumentCaptor<PmsKnowledgeDocumentCreateReqVO> createCaptor =
                ArgumentCaptor.forClass(PmsKnowledgeDocumentCreateReqVO.class);
        verify(documentService).createDocument(createCaptor.capture(), eq(userId));
        PmsKnowledgeDocumentCreateReqVO document = createCaptor.getValue();
        assertEquals(libraryId, document.getLibraryId());
        assertEquals(0L, document.getFolderId());
        assertEquals(0L, document.getParentId());
        assertEquals("2020.01.01 更新日志", document.getTitle());
        assertTrue(document.getContent().contains("2020.01.01 更新日志"));
        verify(documentService, times(0)).updateDocument(any(), eq(userId));
    }

    @Test
    public void testCreateTemplateDocumentList_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> templateService.createTemplateDocumentList(99L, randomLongId(), randomLongId()),
                KNOWLEDGE_LIBRARY_TEMPLATE_NOT_EXISTS);
        verify(documentService, times(0)).createDocument(any(), anyLong());
    }

}
