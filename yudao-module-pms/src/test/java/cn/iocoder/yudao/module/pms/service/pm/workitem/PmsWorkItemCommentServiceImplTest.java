package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.comment.PmsWorkItemCommentSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemCommentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemCommentMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_COMMENT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_COMMENT_REPLY_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link PmsWorkItemCommentServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsWorkItemCommentServiceImpl.class)
public class PmsWorkItemCommentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemCommentServiceImpl commentService;

    @Resource
    private PmsWorkItemCommentMapper commentMapper;

    @MockitoBean
    private PmsWorkItemService workItemService;
    @MockitoBean
    private PmsWorkItemActivityService workItemActivityService;

    @Test
    public void testCreateCommentAndReply_success() {
        // 准备参数
        Long workItemId = randomLongId();
        Long userId = randomLongId();
        when(workItemService.getWritableWorkItem(eq(workItemId), anyLong()))
                .thenReturn(new PmsWorkItemDO().setId(workItemId).setProjectId(randomLongId()));
        PmsWorkItemCommentSaveReqVO rootReqVO = new PmsWorkItemCommentSaveReqVO()
                .setWorkItemId(workItemId).setContent("  主评论  ");

        // 调用
        Long rootId = commentService.createWorkItemComment(rootReqVO, userId);
        Long replyId = commentService.createWorkItemComment(new PmsWorkItemCommentSaveReqVO()
                .setWorkItemId(workItemId).setMainId(rootId).setReplyUserId(userId).setContent("回复"),
                randomLongId());

        // 断言
        assertEquals("  主评论  ", commentMapper.selectById(rootId).getContent());
        assertEquals(rootId, commentMapper.selectById(replyId).getMainId());
        assertEquals(2, commentService.getWorkItemCommentList(workItemId, userId).size());
    }

    @Test
    public void testCreateComment_replyOtherWorkItem() {
        // mock 数据
        PmsWorkItemCommentDO mainComment = randomCommentDO(randomLongId(), randomLongId(), 0L);
        commentMapper.insert(mainComment);
        Long workItemId = randomLongId();
        when(workItemService.getWritableWorkItem(eq(workItemId), anyLong()))
                .thenReturn(new PmsWorkItemDO().setId(workItemId).setProjectId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> commentService.createWorkItemComment(new PmsWorkItemCommentSaveReqVO()
                        .setWorkItemId(workItemId).setMainId(mainComment.getId()).setContent("回复"),
                randomLongId()), WORK_ITEM_COMMENT_REPLY_INVALID);
    }

    @Test
    public void testUpdateComment_notOwner() {
        // mock 数据
        PmsWorkItemCommentDO comment = randomCommentDO(randomLongId(), randomLongId(), 0L);
        commentMapper.insert(comment);

        // 调用，并断言异常
        assertServiceException(() -> commentService.updateWorkItemComment(new PmsWorkItemCommentSaveReqVO()
                        .setId(comment.getId()).setWorkItemId(comment.getWorkItemId()).setContent("修改"),
                randomLongId()), WORK_ITEM_COMMENT_ACCESS_DENIED);
    }

    @Test
    public void testDeleteComment_deleteReplies() {
        // mock 数据
        Long userId = randomLongId();
        PmsWorkItemCommentDO root = randomCommentDO(randomLongId(), userId, 0L);
        commentMapper.insert(root);
        PmsWorkItemCommentDO reply = randomCommentDO(root.getWorkItemId(), randomLongId(), root.getId());
        commentMapper.insert(reply);
        when(workItemService.getWritableWorkItem(root.getWorkItemId(), userId))
                .thenReturn(new PmsWorkItemDO().setId(root.getWorkItemId()).setProjectId(randomLongId()));

        // 调用
        commentService.deleteWorkItemComment(root.getId(), userId);

        // 断言
        assertNull(commentMapper.selectById(root.getId()));
        assertNull(commentMapper.selectById(reply.getId()));
    }

    // ========== 随机对象 ==========

    private PmsWorkItemCommentDO randomCommentDO(Long workItemId, Long userId, Long mainId) {
        return randomPojo(PmsWorkItemCommentDO.class, comment -> comment.setId(null).setWorkItemId(workItemId)
                .setUserId(userId).setMainId(mainId).setContent("评论"));
    }

}
