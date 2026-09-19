package cn.iocoder.yudao.module.oa.service.discussion;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteOptionDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaVoteOptionMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaVoteRecordMapper;
import cn.iocoder.yudao.module.oa.enums.discussion.OaDiscussionTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaDiscussionVoteServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaDiscussionVoteServiceImpl.class)
public class OaDiscussionVoteServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaDiscussionVoteServiceImpl discussionVoteService;

    @Resource
    private OaVoteOptionMapper voteOptionMapper;
    @Resource
    private OaVoteRecordMapper voteRecordMapper;

    @MockBean
    private OaDiscussionService discussionService;

    @Test
    public void testVoteDiscussion_expired() {
        // 准备参数
        Long discussionId = randomLongId();
        OaDiscussionDO discussion = new OaDiscussionDO().setId(discussionId)
                .setType(OaDiscussionTypeEnum.VOTE.getType()).setVoteMultiple(false)
                .setVoteStartTime(LocalDateTime.now().minusDays(2)).setVoteEndTime(LocalDateTime.now().minusDays(1));

        // mock 方法
        when(discussionService.validateDiscussionExists(discussionId)).thenReturn(discussion);

        // 调用，并断言异常
        assertServiceException(() -> discussionVoteService.voteDiscussion(discussionId,
                Collections.singleton(randomLongId()), randomLongId()), DISCUSSION_VOTE_EXPIRED);
        assertEquals(0L, voteRecordMapper.selectCount());
    }

    @Test
    public void testVoteDiscussion_optionFromOtherDiscussion() {
        // mock 数据
        OaVoteOptionDO option = new OaVoteOptionDO().setDiscussionId(randomLongId()).setTitle("其他讨论").setSort(1);
        voteOptionMapper.insert(option);

        // 准备参数
        Long discussionId = randomLongId();
        OaDiscussionDO discussion = new OaDiscussionDO().setId(discussionId)
                .setType(OaDiscussionTypeEnum.VOTE.getType()).setVoteMultiple(false)
                .setVoteStartTime(LocalDateTime.now().minusDays(1)).setVoteEndTime(LocalDateTime.now().plusDays(1));

        // mock 方法
        when(discussionService.validateDiscussionExists(discussionId)).thenReturn(discussion);

        // 调用，并断言异常
        assertServiceException(() -> discussionVoteService.voteDiscussion(discussionId,
                Collections.singleton(option.getId()), randomLongId()), DISCUSSION_VOTE_OPTION_NOT_EXISTS);
        assertEquals(0L, voteRecordMapper.selectCount());
    }

    @Test
    public void testVoteDiscussion_multipleCanAppend() {
        // mock 数据
        Long discussionId = randomLongId();
        Long userId = randomLongId();
        OaDiscussionDO discussion = new OaDiscussionDO().setId(discussionId)
                .setType(OaDiscussionTypeEnum.VOTE.getType()).setVoteMultiple(true)
                .setVoteStartTime(LocalDateTime.now().minusDays(1)).setVoteEndTime(LocalDateTime.now().plusDays(1));
        OaVoteOptionDO firstOption = new OaVoteOptionDO().setDiscussionId(discussionId).setTitle("方案 A").setSort(1);
        OaVoteOptionDO secondOption = new OaVoteOptionDO().setDiscussionId(discussionId).setTitle("方案 B").setSort(2);
        voteOptionMapper.insert(firstOption);
        voteOptionMapper.insert(secondOption);

        // mock 方法
        when(discussionService.validateDiscussionExists(discussionId)).thenReturn(discussion);

        // 调用
        discussionVoteService.voteDiscussion(discussionId, Collections.singleton(firstOption.getId()), userId);
        discussionVoteService.voteDiscussion(discussionId, Collections.singleton(secondOption.getId()), userId);

        // 断言
        assertEquals(2, voteRecordMapper.selectListByDiscussionIdAndUserId(discussionId, userId).size());
    }

    @Test
    public void testVoteDiscussion_multipleCannotRepeatOption() {
        // mock 数据
        Long discussionId = randomLongId();
        Long userId = randomLongId();
        OaDiscussionDO discussion = new OaDiscussionDO().setId(discussionId)
                .setType(OaDiscussionTypeEnum.VOTE.getType()).setVoteMultiple(true)
                .setVoteStartTime(LocalDateTime.now().minusDays(1)).setVoteEndTime(LocalDateTime.now().plusDays(1));
        OaVoteOptionDO firstOption = new OaVoteOptionDO().setDiscussionId(discussionId).setTitle("方案 A").setSort(1);
        OaVoteOptionDO secondOption = new OaVoteOptionDO().setDiscussionId(discussionId).setTitle("方案 B").setSort(2);
        voteOptionMapper.insert(firstOption);
        voteOptionMapper.insert(secondOption);

        // mock 方法
        when(discussionService.validateDiscussionExists(discussionId)).thenReturn(discussion);

        // 调用
        discussionVoteService.voteDiscussion(discussionId, Collections.singleton(firstOption.getId()), userId);
        // 断言
        assertServiceException(() -> discussionVoteService.voteDiscussion(discussionId,
                Arrays.asList(firstOption.getId(), secondOption.getId()), userId), DISCUSSION_VOTE_DUPLICATE);
        assertEquals(1, voteRecordMapper.selectListByDiscussionIdAndUserId(discussionId, userId).size());
    }

}
