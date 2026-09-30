package cn.iocoder.yudao.module.ai1.service.agent;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.agent.Ai1AgentMapper;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.harness.skill.Ai1SkillToolFactory;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.AGENT_DISABLE;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MODEL_TYPE_NOT_CHAT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link Ai1AgentServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1AgentServiceImpl.class)
public class Ai1AgentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1AgentServiceImpl agentService;

    @Resource
    private Ai1AgentMapper agentMapper;

    @MockitoBean
    private Ai1ModelService modelService;
    @MockitoBean
    private Ai1SessionService sessionService;
    @MockitoBean
    private Ai1SkillToolFactory skillToolFactory;

    @Test
    public void testCreateAgent_modelNotChat() {
        // mock 方法：绑定的是嵌入模型
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO()
                .setModelType(Ai1ModelTypeEnum.EMBEDDING.getType()));

        // 调用，并断言异常
        assertServiceException(() -> agentService.createAgent(buildAgentSaveReqVO()), MODEL_TYPE_NOT_CHAT);
    }

    @Test
    public void testCreateAgent_success() {
        // mock 方法
        mockChatModel();

        // 调用
        Long id = agentService.createAgent(buildAgentSaveReqVO().setKnowledgeBaseIds(Arrays.asList(1L, 2L)));

        // 断言：初始关闭；绑定集合按逗号分隔存储并正确读回
        Ai1AgentDO agent = agentMapper.selectById(id);
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), agent.getStatus());
        assertEquals(Arrays.asList(1L, 2L), agent.getKnowledgeBaseIds());
        assertTrue(agent.getMcpIds().isEmpty());
    }

    @Test
    public void testUpdateAgent_clearBindings() {
        // mock 数据
        mockChatModel();
        Long id = agentService.createAgent(buildAgentSaveReqVO().setSkillIds(Arrays.asList(3L)));

        // 调用：绑定集合传空数组，视为清空
        agentService.updateAgent(buildAgentSaveReqVO().setId(id));

        // 断言
        assertTrue(agentMapper.selectById(id).getSkillIds().isEmpty());
    }

    @Test
    public void testUpdateAgentStatus_success() {
        // mock 数据
        mockChatModel();
        Long id = agentService.createAgent(buildAgentSaveReqVO());

        // 调用
        agentService.updateAgentStatus(id, CommonStatusEnum.ENABLE.getStatus());

        // 断言：只修改状态
        Ai1AgentDO agent = agentMapper.selectById(id);
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), agent.getStatus());
        assertEquals("客服助手", agent.getName());
    }

    @Test
    public void testValidateAgentEnabled_disable() {
        // mock 数据
        mockChatModel();
        Long id = agentService.createAgent(buildAgentSaveReqVO());

        // 调用，并断言异常：新建 Agent 默认关闭
        assertServiceException(() -> agentService.validateAgentEnabled(id), AGENT_DISABLE, "客服助手");
    }

    @Test
    public void testGetAgentListByStatus() {
        // mock 数据：一个开启、一个关闭
        mockChatModel();
        Long enabledId = agentService.createAgent(buildAgentSaveReqVO());
        agentService.createAgent(buildAgentSaveReqVO());
        agentService.updateAgentStatus(enabledId, CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<Ai1AgentDO> list = agentService.getAgentListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // 断言：只返回开启的 Agent
        assertEquals(1, list.size());
        assertEquals(enabledId, list.get(0).getId());
    }

    @Test
    public void testDeleteAgent_cascade() {
        // mock 数据
        mockChatModel();
        Long id = agentService.createAgent(buildAgentSaveReqVO());

        // 调用
        agentService.deleteAgent(id);

        // 断言：删除 Agent，级联删除对话，并清理 SKILL 沙箱
        assertNull(agentMapper.selectById(id));
        verify(sessionService).deleteSessionListByAgentIds(Collections.singletonList(id));
        verify(skillToolFactory).evict(id);
    }

    // ========== 随机对象 ==========

    private void mockChatModel() {
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO()
                .setModelType(Ai1ModelTypeEnum.CHAT.getType()));
    }

    private static Ai1AgentSaveReqVO buildAgentSaveReqVO() {
        return new Ai1AgentSaveReqVO().setName("客服助手").setProviderId(1L).setModelId(2L).setSystemPrompt("你是客服")
                .setKnowledgeBaseIds(Collections.emptyList()).setMcpIds(Collections.emptyList()).setSkillIds(Collections.emptyList());
    }

}
