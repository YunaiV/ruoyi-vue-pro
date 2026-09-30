package cn.iocoder.yudao.module.ai1.service.session;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.session.Ai1SessionMapper;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.SESSION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link Ai1SessionServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1SessionServiceImpl.class)
public class Ai1SessionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1SessionServiceImpl sessionService;

    @Resource
    private Ai1SessionMapper sessionMapper;

    @MockitoBean
    private Ai1AgentService agentService;
    @MockitoBean
    private Ai1SessionMessageService sessionMessageService;

    @Test
    public void testCreateSessionMy_success() {
        // 调用
        Long id = sessionService.createSessionMy(100L, new Ai1SessionCreateMyReqVO().setAgentId(1L));

        // 断言：归属当前用户，使用默认标题
        Ai1SessionDO session = sessionMapper.selectById(id);
        assertEquals(100L, session.getUserId());
        assertEquals(Ai1SessionDO.TITLE_DEFAULT, session.getTitle());
        verify(agentService).validateAgentEnabled(1L);
    }

    @Test
    public void testUpdateSessionMy_otherUser() {
        // mock 数据：他人的对话
        Long id = sessionService.createSessionMy(100L, new Ai1SessionCreateMyReqVO().setAgentId(1L));

        // 调用，并断言异常：不能改他人对话，且与不存在同样处理
        assertServiceException(() -> sessionService.updateSessionMy(200L,
                new Ai1SessionUpdateMyReqVO().setId(id).setTitle("改标题")), SESSION_NOT_EXISTS);
        assertEquals(Ai1SessionDO.TITLE_DEFAULT, sessionMapper.selectById(id).getTitle());
    }

    @Test
    public void testDeleteSessionMy_otherUser() {
        // mock 数据：他人的对话
        Long id = sessionService.createSessionMy(100L, new Ai1SessionCreateMyReqVO().setAgentId(1L));

        // 调用，并断言异常
        assertServiceException(() -> sessionService.deleteSessionMy(200L, id), SESSION_NOT_EXISTS);
        assertNotNull(sessionMapper.selectById(id));
        verifyNoInteractions(sessionMessageService);
    }

    @Test
    public void testDeleteSessionMy_success() {
        // mock 数据
        Long id = sessionService.createSessionMy(100L, new Ai1SessionCreateMyReqVO().setAgentId(1L));

        // 调用
        sessionService.deleteSessionMy(100L, id);

        // 断言：连带删除消息
        assertNull(sessionMapper.selectById(id));
        verify(sessionMessageService).deleteSessionMessageListBySessionIds(Collections.singletonList(id));
    }

    @Test
    public void testGetSessionListByAgentIdAndUserId() {
        // mock 数据：当前用户 2 个、他人 1 个、其他 Agent 1 个
        Long id1 = sessionService.createSessionMy(100L, new Ai1SessionCreateMyReqVO().setAgentId(1L));
        Long id2 = sessionService.createSessionMy(100L, new Ai1SessionCreateMyReqVO().setAgentId(1L));
        sessionService.createSessionMy(200L, new Ai1SessionCreateMyReqVO().setAgentId(1L));
        sessionService.createSessionMy(100L, new Ai1SessionCreateMyReqVO().setAgentId(2L));

        // 调用
        List<Ai1SessionDO> list = sessionService.getSessionListByAgentIdAndUserId(1L, 100L);

        // 断言：只返回当前用户在该 Agent 下的对话，按编号倒序
        assertEquals(Arrays.asList(id2, id1), list.stream().map(Ai1SessionDO::getId).toList());
    }

}
