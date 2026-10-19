package cn.iocoder.yudao.module.oa.service.discussion;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionLikeDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaDiscussionLikeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaDiscussionLikeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaDiscussionLikeServiceImpl.class)
public class OaDiscussionLikeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaDiscussionLikeServiceImpl discussionLikeService;

    @Resource
    private OaDiscussionLikeMapper discussionLikeMapper;
    @Resource
    private DataSource dataSource;

    @MockBean
    private OaDiscussionService discussionService;
    @MockBean
    private OaDiscussionReplyService discussionReplyService;

    @Test
    public void testCreateAndDeleteDiscussionLike_reply() {
        // 准备参数
        Long replyId = randomLongId();
        Long userId = randomLongId();

        // mock 方法
        when(discussionReplyService.validateDiscussionReplyExists(replyId))
                .thenReturn(new OaDiscussionReplyDO().setId(replyId)
                        .setParentId(OaDiscussionReplyDO.PARENT_ID_ROOT));

        // 调用
        discussionLikeService.createDiscussionLike(null, replyId, userId);
        discussionLikeService.createDiscussionLike(null, replyId, userId);
        // 断言
        assertEquals(1L, discussionLikeMapper.selectCount());
        discussionLikeService.deleteDiscussionLike(null, replyId, userId);
        discussionLikeService.deleteDiscussionLike(null, replyId, userId);
        assertEquals(0L, discussionLikeMapper.selectCount());
        verify(discussionReplyService, times(2)).validateDiscussionReplyExists(replyId);
        verifyNoInteractions(discussionService);
    }

    @Test
    public void testDeleteDiscussionLikesByReplyIds_empty() {
        // mock 数据
        OaDiscussionLikeDO like = new OaDiscussionLikeDO().setDiscussionId(randomLongId()).setUserId(randomLongId());
        discussionLikeMapper.insert(like);

        // 调用
        discussionLikeService.deleteDiscussionLikesByReplyIds(Collections.emptyList());

        // 断言
        assertNotNull(discussionLikeMapper.selectById(like.getId()));
    }

    @Test
    public void testCreateAndDeleteDiscussionLike_preservesHistory() {
        // 准备参数
        Long replyId = randomLongId();
        Long userId = randomLongId();

        // mock 方法
        when(discussionReplyService.validateDiscussionReplyExists(replyId))
                .thenReturn(new OaDiscussionReplyDO().setId(replyId)
                        .setParentId(OaDiscussionReplyDO.PARENT_ID_ROOT));

        // 调用：连续两轮点赞和取消，逻辑删除历史不应触发唯一键冲突
        for (int i = 0; i < 2; i++) {
            discussionLikeService.createDiscussionLike(null, replyId, userId);
            discussionLikeService.deleteDiscussionLike(null, replyId, userId);
        }

        // 断言：业务查询不可见，物理记录仍保留
        assertEquals(0L, discussionLikeMapper.selectCount());
        assertEquals(2, new JdbcTemplate(dataSource).queryForObject(
                "SELECT COUNT(*) FROM oa_discussion_like WHERE reply_id = ? AND deleted = TRUE", Integer.class, replyId));
    }

    @Test
    public void testCreateDiscussionLike_childReply() {
        // 准备参数
        Long replyId = randomLongId();

        // mock 方法
        when(discussionReplyService.validateDiscussionReplyExists(replyId))
                .thenReturn(new OaDiscussionReplyDO().setId(replyId).setParentId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> discussionLikeService.createDiscussionLike(null, replyId, randomLongId()),
                DISCUSSION_LIKE_TARGET_INVALID);
        assertEquals(0L, discussionLikeMapper.selectCount());
    }

}
