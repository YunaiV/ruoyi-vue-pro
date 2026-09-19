package cn.iocoder.yudao.module.oa.service.discussion;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyCreateReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaDiscussionReplyMapper;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaDiscussionReplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaDiscussionReplyServiceImpl.class)
public class OaDiscussionReplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaDiscussionReplyServiceImpl discussionReplyService;

    @Resource
    private OaDiscussionReplyMapper discussionReplyMapper;

    @MockitoBean
    private OaDiscussionService discussionService;
    @MockitoBean
    private OaDiscussionLikeService discussionLikeService;
    @MockitoBean
    private PermissionApi permissionApi;

    @Test
    public void testCreateDiscussionReply_parentFromOtherDiscussion() {
        // mock 数据
        OaDiscussionReplyDO parent = new OaDiscussionReplyDO().setDiscussionId(randomLongId())
                .setUserId(randomLongId()).setParentId(OaDiscussionReplyDO.PARENT_ID_ROOT).setContent("父回复");
        discussionReplyMapper.insert(parent);

        // 准备参数
        Long discussionId = randomLongId();
        OaDiscussionReplyCreateReqVO reqVO = new OaDiscussionReplyCreateReqVO()
                .setDiscussionId(discussionId).setParentId(parent.getId()).setContent("回复");

        // mock 方法
        when(discussionService.validateDiscussionExists(discussionId)).thenReturn(new OaDiscussionDO().setId(discussionId));

        // 调用，并断言异常
        assertServiceException(() -> discussionReplyService.createDiscussionReply(reqVO, randomLongId()),
                DISCUSSION_REPLY_NOT_EXISTS);
        assertEquals(1L, discussionReplyMapper.selectCount());
    }

    @Test
    public void testCreateDiscussionReply_longRootContent() {
        // 准备参数：根回复对齐源端 TEXT，不再受原来 2000 字限制
        Long discussionId = randomLongId();
        String content = StrUtil.repeat("回复正文", 600);
        OaDiscussionReplyCreateReqVO reqVO = new OaDiscussionReplyCreateReqVO()
                .setDiscussionId(discussionId).setParentId(OaDiscussionReplyDO.PARENT_ID_ROOT).setContent(content);
        // mock 方法
        when(discussionService.validateDiscussionExists(discussionId))
                .thenReturn(new OaDiscussionDO().setId(discussionId));

        // 调用
        Long id = discussionReplyService.createDiscussionReply(reqVO, randomLongId());
        // 断言
        assertEquals(content, discussionReplyMapper.selectById(id).getContent());
    }

    @Test
    public void testDeleteDiscussionRepliesByDiscussionId() {
        // mock 数据
        Long discussionId = randomLongId();
        OaDiscussionReplyDO reply = new OaDiscussionReplyDO().setDiscussionId(discussionId)
                .setUserId(randomLongId()).setParentId(OaDiscussionReplyDO.PARENT_ID_ROOT).setContent("回复");
        discussionReplyMapper.insert(reply);

        // 调用
        discussionReplyService.deleteDiscussionRepliesByDiscussionId(discussionId);

        // 断言
        assertNull(discussionReplyMapper.selectById(reply.getId()));
        verify(discussionLikeService).deleteDiscussionLikesByReplyIds(Collections.singleton(reply.getId()));
    }

    @Test
    public void testDeleteDiscussionReply_cyclicParents() {
        // mock 数据：两条回复错误地互为父回复
        Long discussionId = randomLongId();
        Long userId = randomLongId();
        OaDiscussionReplyDO root = new OaDiscussionReplyDO().setDiscussionId(discussionId)
                .setUserId(userId).setParentId(OaDiscussionReplyDO.PARENT_ID_ROOT).setContent("根回复");
        discussionReplyMapper.insert(root);
        OaDiscussionReplyDO child = new OaDiscussionReplyDO().setDiscussionId(discussionId)
                .setUserId(userId).setParentId(root.getId()).setContent("子回复");
        discussionReplyMapper.insert(child);
        discussionReplyMapper.updateById(new OaDiscussionReplyDO().setId(root.getId()).setParentId(child.getId()));

        // mock 方法
        when(discussionService.validateDiscussionExists(discussionId))
                .thenReturn(new OaDiscussionDO().setId(discussionId).setUserId(userId));

        // 调用
        discussionReplyService.deleteDiscussionReply(root.getId(), userId);

        // 断言：循环关联不会导致死循环，也不会漏掉子回复
        assertNull(discussionReplyMapper.selectById(root.getId()));
        assertNull(discussionReplyMapper.selectById(child.getId()));
    }

}
