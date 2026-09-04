package cn.iocoder.yudao.module.pms.service.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentLabelDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentLabelMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.content.PmsKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_LABEL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeDocumentLabelServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeDocumentLabelServiceImpl.class)
public class PmsKnowledgeDocumentLabelServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeDocumentLabelServiceImpl documentLabelService;

    @Resource
    private PmsKnowledgeDocumentLabelMapper documentLabelMapper;
    @Resource
    private PmsKnowledgeDocumentMapper documentMapper;

    @MockitoBean
    private PmsKnowledgeLibraryMemberService libraryMemberService;
    @MockitoBean
    private PmsKnowledgeContentPermissionService contentPermissionService;

    @Test
    public void testCreateDocumentLabel_success() {
        // 准备参数
        PmsKnowledgeDocumentLabelSaveReqVO reqVO = new PmsKnowledgeDocumentLabelSaveReqVO()
                .setName("  产品  ").setColor("  #409EFF  ");

        // 调用
        Long id = documentLabelService.createDocumentLabel(reqVO);

        // 断言
        PmsKnowledgeDocumentLabelDO documentLabel = documentLabelMapper.selectById(id);
        assertEquals("产品", documentLabel.getName());
        assertEquals("#409EFF", documentLabel.getColor());
    }

    @Test
    public void testUpdateDocumentLabel_notExists() {
        // 准备参数
        PmsKnowledgeDocumentLabelSaveReqVO reqVO = new PmsKnowledgeDocumentLabelSaveReqVO()
                .setId(randomLongId()).setName("产品").setColor("#409EFF");

        // 调用，并断言异常
        assertServiceException(() -> documentLabelService.updateDocumentLabel(reqVO),
                KNOWLEDGE_DOCUMENT_LABEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteDocumentLabel_success() {
        // mock 数据
        PmsKnowledgeDocumentLabelDO documentLabel = randomDocumentLabelDO();
        documentLabelMapper.insert(documentLabel);

        // 调用
        documentLabelService.deleteDocumentLabel(documentLabel.getId());

        // 断言
        assertNull(documentLabelMapper.selectById(documentLabel.getId()));
    }

    @Test
    public void testGetDocumentLabel_success() {
        // mock 数据
        PmsKnowledgeDocumentLabelDO documentLabel = randomDocumentLabelDO();
        documentLabelMapper.insert(documentLabel);

        // 调用
        PmsKnowledgeDocumentLabelDO result = documentLabelService.getDocumentLabel(documentLabel.getId());

        // 断言
        assertEquals(documentLabel.getId(), result.getId());
        assertEquals(documentLabel.getName(), result.getName());
        assertEquals(documentLabel.getColor(), result.getColor());
    }

    @Test
    public void testGetDocumentPageByLabel_success() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        PmsKnowledgeDocumentLabelDO targetLabel = randomDocumentLabelDO();
        documentLabelMapper.insert(targetLabel);
        PmsKnowledgeDocumentLabelDO otherLabel = randomDocumentLabelDO();
        documentLabelMapper.insert(otherLabel);
        PmsKnowledgeDocumentDO targetDocument = randomDocumentDO(libraryId,
                Arrays.asList(targetLabel.getId(), otherLabel.getId()));
        PmsKnowledgeDocumentDO otherDocument = randomDocumentDO(libraryId,
                Collections.singletonList(otherLabel.getId()));
        PmsKnowledgeDocumentDO unreadableDocument = randomDocumentDO(libraryId,
                Collections.singletonList(targetLabel.getId()));
        documentMapper.insert(targetDocument);
        documentMapper.insert(otherDocument);
        documentMapper.insert(unreadableDocument);
        when(libraryMemberService.getReadableLibraryIdList(userId)).thenReturn(Collections.singletonList(libraryId));
        when(contentPermissionService.getReadableContentPermissionIdSet(Collections.singletonList(libraryId), userId))
                .thenReturn(new HashSet<>(Arrays.asList(targetDocument.getPermissionId(), otherDocument.getPermissionId())));
        // 准备参数
        PmsKnowledgeDocumentLabelPageReqVO pageReqVO = new PmsKnowledgeDocumentLabelPageReqVO()
                .setLabelId(targetLabel.getId());
        pageReqVO.setPageNo(1).setPageSize(10);

        // 调用
        PageResult<PmsKnowledgeDocumentDO> pageResult =
                documentLabelService.getDocumentPageByLabel(pageReqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(targetLabel.getId(), CollUtil.getFirst(CollUtil.getFirst(pageResult.getList()).getLabelIds()));
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeDocumentLabelDO randomDocumentLabelDO() {
        return randomPojo(PmsKnowledgeDocumentLabelDO.class,
                documentLabel -> documentLabel.setId(null).setName("产品").setColor("#409EFF"));
    }

    private PmsKnowledgeDocumentDO randomDocumentDO(Long libraryId, List<Long> labelIds) {
        return randomPojo(PmsKnowledgeDocumentDO.class, document -> document.setId(null).setLibraryId(libraryId)
                .setPermissionId(randomLongId())
                .setFolderId(0L).setParentId(0L).setTitle("测试文档")
                .setType(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType())
                .setStatus(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus()).setLabelIds(labelIds));
    }

}
