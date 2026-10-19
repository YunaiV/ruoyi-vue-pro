package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeDocumentLikeMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeDocumentLikeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeDocumentLikeServiceImpl.class)
public class PmsKnowledgeDocumentLikeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeDocumentLikeServiceImpl documentLikeService;

    @Resource
    private PmsKnowledgeDocumentLikeMapper documentLikeMapper;

    @MockBean
    private PmsKnowledgeInteractionTargetService interactionTargetService;

    @Test
    public void testCreateDocumentLike_idempotent() {
        // mock 数据
        Long userId = randomLongId();
        Long documentId = randomLongId();
        when(interactionTargetService.validateTargetReadable(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId, userId)).thenReturn(randomLongId());

        // 调用
        documentLikeService.createDocumentLike(documentId, userId);
        documentLikeService.createDocumentLike(documentId, userId);

        // 断言
        assertNotNull(documentLikeMapper.selectByDocumentIdAndUserId(documentId, userId));
    }

    @Test
    public void testDeleteDocumentLike_idempotent() {
        // mock 数据
        Long userId = randomLongId();
        Long documentId = randomLongId();
        when(interactionTargetService.validateTargetReadable(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId, userId)).thenReturn(randomLongId());
        documentLikeService.createDocumentLike(documentId, userId);

        // 调用
        documentLikeService.deleteDocumentLike(documentId, userId);
        documentLikeService.deleteDocumentLike(documentId, userId);

        // 断言
        assertNull(documentLikeMapper.selectByDocumentIdAndUserId(documentId, userId));
    }

    @Test
    public void testDeleteLikesByDocumentIds() {
        // mock 数据
        Long userId = randomLongId();
        Long documentId = randomLongId();
        when(interactionTargetService.validateTargetReadable(
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId, userId)).thenReturn(randomLongId());
        documentLikeService.createDocumentLike(documentId, userId);

        // 调用
        documentLikeService.deleteLikesByDocumentIds(Collections.singleton(documentId));

        // 断言
        assertTrue(CollUtil.isEmpty(documentLikeMapper.selectListByDocumentId(documentId)));
    }

}
