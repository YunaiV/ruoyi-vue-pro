package cn.iocoder.yudao.module.ai1.harness.memory;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.enums.session.Ai1SessionMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmChatTool;
import cn.iocoder.yudao.module.ai1.service.memory.Ai1MemoryService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MODEL_TYPE_NOT_CHAT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1MemoryTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1MemoryToolTest extends BaseMockitoUnitTest {

    @InjectMocks
    private Ai1MemoryTool memoryTool;

    @Mock
    private Ai1MemoryService memoryService;
    @Mock
    private Ai1ModelService modelService;
    @Mock
    private Ai1LlmChatTool llmChatTool;

    @Test
    public void testLoadMemory() {
        // mock 方法
        when(memoryService.getMemoryByAgentIdAndUserId(1L, 100L)).thenReturn(Ai1MemoryDO.builder().content("## 用户画像\n- 程序员").build());

        // 调用，并断言
        assertEquals("## 用户画像\n- 程序员", memoryTool.loadMemory(1L, 100L));
        // 调用，并断言：编号为空时不查询
        assertNull(memoryTool.loadMemory(null, 100L));
        verify(memoryService, never()).getMemoryByAgentIdAndUserId(isNull(), any());
    }

    @Test
    public void testMergePrompt() {
        // 调用，并断言：按模板合入记忆
        assertEquals("你是客服\n\n以下是该用户的历史长期记忆（Markdown），请在回答时结合这些信息；"
                + "若与用户当前表述冲突，以当前表述为准：\n## 用户画像\n- 程序员", memoryTool.mergePrompt("你是客服", "  ## 用户画像\n- 程序员 \n"));
        // 调用，并断言：记忆为空时原样返回
        assertEquals("你是客服", memoryTool.mergePrompt("你是客服", " "));
        assertEquals("", memoryTool.mergePrompt(null, null));
    }

    @Test
    public void testGenerate_modelNotChat() {
        // mock modelService 的方法
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO()
                .setModelType(Ai1ModelTypeEnum.EMBEDDING.getType()));

        // 调用，并断言异常
        assertServiceException(() -> memoryTool.generate(buildAgent(), null, List.of(), 2000), MODEL_TYPE_NOT_CHAT);
        verifyNoInteractions(llmChatTool);
    }

    @Test
    public void testGenerate_prompt() {
        // mock 方法
        mockChat("## 用户画像\n- 程序员");
        // 准备参数
        List<Ai1SessionMessageDO> messages = List.of(
                new Ai1SessionMessageDO().setRole(Ai1SessionMessageRoleEnum.USER.getRole()).setContent("我是程序员"),
                new Ai1SessionMessageDO().setRole(Ai1SessionMessageRoleEnum.ASSISTANT.getRole()).setContent("好的"));

        // 调用
        String memory = memoryTool.generate(buildAgent(), " 旧记忆 ", messages, 500);
        // 断言：单轮会话，不带历史、工具、RAG，也不替换会话 Header
        assertEquals("## 用户画像\n- 程序员", memory);
        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(llmChatTool).chat(any(), systemPromptCaptor.capture(), eq(List.of()), userPromptCaptor.capture(),
                eq(List.of()), eq(List.of()), isNull(), isNull(), isNull());
        assertTrue(systemPromptCaptor.getValue().endsWith("总长度不超过 500 字。"));
        assertEquals("【历史记忆】\n旧记忆\n\n【最新对话】\n用户：我是程序员\n助手：好的\n", userPromptCaptor.getValue());
    }

    @Test
    public void testGenerate_normalizeSections() {
        // mock 方法：标题与内容压成一行
        mockChat("##用户画像-后端工程师##偏好习惯-简洁回答");

        // 调用
        String memory = memoryTool.generate(buildAgent(), null, List.of(), 2000);
        // 断言
        assertEquals("## 用户画像\n- 后端工程师\n\n## 偏好习惯\n- 简洁回答", memory);
    }

    @Test
    public void testGenerate_defaultMaxLength() {
        // mock 方法
        mockChat("用户喜欢😀表情");

        // 调用：未配置长度上限时使用默认值
        String memory = memoryTool.generate(buildAgent(), null, List.of(), 0);
        // 断言
        assertEquals("用户喜欢😀表情", memory);
        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(llmChatTool).chat(any(), systemPromptCaptor.capture(), anyList(), anyString(), anyList(), anyList(),
                isNull(), isNull(), isNull());
        assertTrue(systemPromptCaptor.getValue().endsWith("总长度不超过 2000 字。"));
        // 调用，并断言：按长度上限截断
        assertEquals("用户喜欢", memoryTool.generate(buildAgent(), null, List.of(), 5));
    }

    @Test
    public void testGenerate_empty() {
        // mock 方法
        mockChat("  ");

        // 调用，并断言：模型无输出时返回空，由调用方跳过写入
        assertTrue(memoryTool.generate(buildAgent(), null, List.of(), 2000).isBlank());
    }

    // ========== mock 方法 ==========

    private void mockChat(String content) {
        when(modelService.getModelRespBO(1L, 2L)).thenReturn(new Ai1ModelRespBO()
                .setModelType(Ai1ModelTypeEnum.CHAT.getType()));
        when(llmChatTool.chat(any(), anyString(), anyList(), anyString(), anyList(), anyList(), isNull(), isNull(), isNull()))
                .thenReturn(new Ai1LlmChatTool.ChatText(content, ""));
    }

    // ========== 测试数据 ==========

    private static Ai1AgentDO buildAgent() {
        return new Ai1AgentDO().setId(1L).setProviderId(1L).setModelId(2L);
    }

}
