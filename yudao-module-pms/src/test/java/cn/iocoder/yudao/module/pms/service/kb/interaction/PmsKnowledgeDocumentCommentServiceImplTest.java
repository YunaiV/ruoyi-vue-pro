package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.comment.PmsKnowledgeDocumentCommentSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentCommentDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeDocumentCommentMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_COMMENT_REPLY_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_DOCUMENT_COMMENT_TYPE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeDocumentCommentServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeDocumentCommentServiceImpl.class)
public class PmsKnowledgeDocumentCommentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeDocumentCommentServiceImpl commentService;

    @Resource
    private PmsKnowledgeDocumentCommentMapper commentMapper;

    @MockBean
    private PmsKnowledgeDocumentService documentService;

    @Test
    public void testCreateComment_reply() {
        // mock 数据
        Long documentId = randomLongId();
        Long userId = randomLongId();
        mockRichTextDocument(documentId);
        Long mainId = commentService.createDocumentComment(new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(documentId).setContent("主评论"), userId);

        // 准备参数
        PmsKnowledgeDocumentCommentSaveReqVO reqVO = new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(documentId).setMainId(mainId).setReplyUserId(userId).setContent("回复内容");

        // 调用
        Long replyId = commentService.createDocumentComment(reqVO, randomLongId());

        // 断言
        PmsKnowledgeDocumentCommentDO reply = commentMapper.selectById(replyId);
        assertEquals(mainId, reply.getMainId());
        assertEquals("回复内容", reply.getContent());
    }

    @Test
    public void testCreateComment_replyDifferentDocument() {
        // mock 数据
        Long mainDocumentId = randomLongId();
        Long targetDocumentId = randomLongId();
        mockRichTextDocument(mainDocumentId);
        mockRichTextDocument(targetDocumentId);
        Long mainId = commentService.createDocumentComment(new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(mainDocumentId).setContent("主评论"), randomLongId());

        // 准备参数
        PmsKnowledgeDocumentCommentSaveReqVO reqVO = new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(targetDocumentId).setMainId(mainId).setContent("非法回复");

        // 调用并断言
        assertServiceException(() -> commentService.createDocumentComment(reqVO, randomLongId()),
                KNOWLEDGE_DOCUMENT_COMMENT_REPLY_INVALID);
    }

    @Test
    public void testDeleteComment_deletesReplies() {
        // mock 数据
        Long documentId = randomLongId();
        Long userId = randomLongId();
        mockRichTextDocument(documentId);
        Long mainId = commentService.createDocumentComment(new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(documentId).setContent("主评论"), userId);
        commentService.createDocumentComment(new PmsKnowledgeDocumentCommentSaveReqVO().setDocumentId(documentId)
                .setMainId(mainId).setReplyUserId(userId).setContent("回复"), randomLongId());

        // 调用
        commentService.deleteDocumentComment(mainId, userId);

        // 断言
        assertTrue(CollUtil.isEmpty(commentMapper.selectListByDocumentId(documentId)));
    }

    @Test
    public void testGetCommentList() {
        // mock 数据
        Long documentId = randomLongId();
        mockRichTextDocument(documentId);
        commentService.createDocumentComment(new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(documentId).setContent("第一条"), randomLongId());
        commentService.createDocumentComment(new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(documentId).setContent("第二条"), randomLongId());

        // 调用
        List<PmsKnowledgeDocumentCommentDO> comments = commentService.getDocumentCommentList(documentId, randomLongId());

        // 断言
        assertEquals(2, comments.size());
    }

    @Test
    public void testCreateComment_fileDocument() {
        // mock 数据
        Long documentId = randomLongId();
        Long userId = randomLongId();
        when(documentService.getDocument(documentId, userId)).thenReturn(new PmsKnowledgeDocumentDO()
                .setId(documentId).setType(PmsKnowledgeDocumentTypeEnum.FILE.getType()));

        // 准备参数
        PmsKnowledgeDocumentCommentSaveReqVO reqVO = new PmsKnowledgeDocumentCommentSaveReqVO()
                .setDocumentId(documentId).setContent("文件评论");

        // 调用并断言
        assertServiceException(() -> commentService.createDocumentComment(reqVO, userId),
                KNOWLEDGE_DOCUMENT_COMMENT_TYPE_INVALID);
    }

    // ========== Mock 方法 ==========

    private void mockRichTextDocument(Long documentId) {
        when(documentService.getDocument(eq(documentId), anyLong())).thenReturn(new PmsKnowledgeDocumentDO()
                        .setId(documentId).setType(PmsKnowledgeDocumentTypeEnum.RICH_TEXT.getType()));
    }

}
