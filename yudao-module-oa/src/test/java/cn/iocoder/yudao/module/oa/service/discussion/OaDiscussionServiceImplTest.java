package cn.iocoder.yudao.module.oa.service.discussion;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaDiscussionMapper;
import cn.iocoder.yudao.module.oa.enums.discussion.OaDiscussionTypeEnum;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaDiscussionServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaDiscussionServiceImpl.class)
public class OaDiscussionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaDiscussionServiceImpl discussionService;

    @Resource
    private OaDiscussionMapper discussionMapper;

    @MockBean
    private OaDiscussionReplyService discussionReplyService;
    @MockBean
    private OaDiscussionLikeService discussionLikeService;
    @MockBean
    private OaDiscussionVoteService discussionVoteService;
    @MockBean
    private PermissionApi permissionApi;

    @Test
    public void testGetDiscussion_notExists() {

        // 调用，并断言：纯查询不抛出异常
        assertNull(discussionService.getDiscussion(randomLongId()));
    }

    @Test
    public void testValidateDiscussionExists_notExists() {

        // 调用，并断言：存在性校验抛出业务异常
        assertServiceException(() -> discussionService.validateDiscussionExists(randomLongId()), DISCUSSION_NOT_EXISTS);
    }

    @Test
    public void testDeleteDiscussion_cascadeThroughServices() {
        // mock 数据
        Long userId = randomLongId();
        OaDiscussionDO discussion = new OaDiscussionDO().setUserId(userId)
                .setType(OaDiscussionTypeEnum.DISCUSSION.getType()).setTitle("讨论主题").setVisitCount(0);
        discussionMapper.insert(discussion);

        // 调用
        discussionService.deleteDiscussion(discussion.getId(), userId);

        // 断言
        assertNull(discussionMapper.selectById(discussion.getId()));
        verify(discussionReplyService).deleteDiscussionRepliesByDiscussionId(discussion.getId());
        verify(discussionLikeService).deleteDiscussionLikesByDiscussionId(discussion.getId());
        verify(discussionVoteService).deleteDiscussionVotesByDiscussionId(discussion.getId());
    }

}
