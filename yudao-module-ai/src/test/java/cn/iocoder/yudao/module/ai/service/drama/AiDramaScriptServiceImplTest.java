package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaScriptMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.ResponseFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiDramaScriptServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiDramaScriptServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long SCRIPT_ID = 200L;
    private static final Long CHAT_MODEL_ID = 10L;

    @Spy
    @InjectMocks
    private AiDramaScriptServiceImpl scriptService;

    @Mock
    private AiDramaScriptMapper scriptMapper;
    @Mock
    private AiDramaProjectService projectService;
    @Mock
    private AiDramaAssetService assetService;
    @Mock
    private AiDramaStoryboardService storyboardService;
    @Mock
    private AiModelService modelService;

    // ========== parseJson ==========

    @Test
    public void testParseJson_markdownFence() {
        JsonNode node = AiDramaScriptServiceImpl.parseJson("生成剧本",
                "好的，以下是剧本：\n```json\n{\"title\":\"末班地铁\",\"content\":\"第一场\"}\n```");
        assertEquals("末班地铁", node.get("title").asText());

        JsonNode array = AiDramaScriptServiceImpl.parseJson("拆分分镜", "[{\"sceneNo\":1}]");
        assertTrue(array.isArray());
    }

    @Test
    public void testParseJson_invalid() {
        assertServiceException(() -> AiDramaScriptServiceImpl.parseJson("生成剧本", "抱歉，我无法完成"),
                DRAMA_CHAT_FAIL, "生成剧本", "模型返回的内容不是 JSON");
        assertServiceException(() -> AiDramaScriptServiceImpl.parseJson("生成剧本", "{\"title\": }"),
                DRAMA_CHAT_FAIL, "生成剧本", "模型返回的内容不是 JSON");
    }

    // ========== 生成、打磨 ==========

    @Test
    public void testGenerateScriptMy_createWhenNotExists() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockChat("{\"title\":\"末班地铁\",\"tone\":\"悬疑\",\"outline\":\"梗概\",\"content\":\"第一场 车厢\"}");
        doAnswer(invocation -> {
            ((AiDramaScriptDO) invocation.getArgument(0)).setId(SCRIPT_ID);
            return 1;
        }).when(scriptMapper).insert(any(AiDramaScriptDO.class));

        scriptService.generateScriptMy(USER_ID, chatReqVO());

        ArgumentCaptor<AiDramaScriptDO> captor = ArgumentCaptor.forClass(AiDramaScriptDO.class);
        verify(scriptMapper).insert(captor.capture());
        assertEquals(PROJECT_ID, captor.getValue().getProjectId());
        assertEquals("末班地铁", captor.getValue().getTitle());
        assertEquals("第一场 车厢", captor.getValue().getContent());
    }

    @Test
    public void testGenerateScriptMy_deepSeekJsonObject() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        ChatModel chatModel = mockChat("{\"content\":\"第一场 车厢\"}", AiPlatformEnum.DEEPSEEK);

        scriptService.generateScriptMy(USER_ID, chatReqVO());

        // DeepSeek：要求返回 JSON 对象
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        DeepSeekChatOptions options = (DeepSeekChatOptions) captor.getValue().getOptions();
        assertEquals(ResponseFormat.Type.JSON_OBJECT, options.getResponseFormat().getType());
    }

    @Test
    public void testGenerateScriptMy_noContent() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockChat("{\"title\":\"末班地铁\"}");

        assertServiceException(() -> scriptService.generateScriptMy(USER_ID, chatReqVO()),
                DRAMA_CHAT_FAIL, "生成剧本", "模型没有返回剧本正文");
        verify(scriptMapper, never()).insert(any(AiDramaScriptDO.class));
    }

    @Test
    public void testPolishScriptMy_appendRevisionNote() {
        when(scriptMapper.selectByProjectId(PROJECT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID)
                .setProjectId(PROJECT_ID).setContent("旧剧本")
                .setRevisionNotes(Collections.singletonList(new AiDramaScriptDO.RevisionNote().setNote("第一次"))));
        mockChat("{\"content\":\"新剧本\"}");
        AiDramaScriptPolishReqVO reqVO = new AiDramaScriptPolishReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setNote("结尾加反转");

        scriptService.polishScriptMy(USER_ID, reqVO);

        ArgumentCaptor<AiDramaScriptDO> captor = ArgumentCaptor.forClass(AiDramaScriptDO.class);
        verify(scriptMapper).updateById(captor.capture());
        assertEquals(SCRIPT_ID, captor.getValue().getId());
        assertEquals("新剧本", captor.getValue().getContent());
        assertEquals(2, captor.getValue().getRevisionNotes().size());
        assertEquals("结尾加反转", captor.getValue().getRevisionNotes().get(1).getNote());
        assertNotNull(captor.getValue().getRevisionNotes().get(1).getTime());
    }

    @Test
    public void testPolishScriptMy_scriptNotExists() {
        AiDramaScriptPolishReqVO reqVO = new AiDramaScriptPolishReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setNote("结尾加反转");

        assertServiceException(() -> scriptService.polishScriptMy(USER_ID, reqVO), DRAMA_SCRIPT_NOT_EXISTS);
    }

    // ========== 分析资产 ==========

    @Test
    public void testAnalyzeAssetListMy_skipExisting() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(
                Collections.singletonList(new AiDramaCharacterDO().setId(1L).setName("林夏")));
        mockChat("{\"characters\":[{\"name\":\"林夏\"},{\"name\":\"老周\",\"description\":\"列车长\"}],"
                + "\"locations\":[{\"name\":\"车厢\"}],\"props\":[{\"title\":\"怀表\"},{\"title\":\" \"}]}");

        scriptService.analyzeAssetListMy(USER_ID, chatReqVO());

        // 角色：只新增老周（第 1 个形象由资产 Service 创建）
        ArgumentCaptor<AiDramaCharacterDO> characterCaptor = ArgumentCaptor.forClass(AiDramaCharacterDO.class);
        verify(assetService, times(1)).createCharacter(characterCaptor.capture());
        assertEquals("老周", characterCaptor.getValue().getName());
        assertEquals("列车长", characterCaptor.getValue().getDescription());
        assertEquals(PROJECT_ID, characterCaptor.getValue().getProjectId());
        // 场景、道具：空名称跳过
        verify(assetService, times(1)).createLocation(any(AiDramaLocationDO.class));
        verify(assetService, times(1)).createVisualAsset(any(AiDramaVisualAssetDO.class));
    }

    // ========== 拆分分镜 ==========

    @Test
    public void testGenerateStoryboardListMy_running() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Collections.singletonList(
                new AiDramaStoryboardDO().setId(1L).setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())));

        assertServiceException(() -> scriptService.generateStoryboardListMy(USER_ID, chatReqVO()), DRAMA_TASK_RUNNING);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGenerateStoryboardListMy_success() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        when(assetService.getCharacterList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaCharacterDO().setId(1L).setName("林夏"), new AiDramaCharacterDO().setId(2L).setName("老周")));
        when(assetService.getAppearanceListByCharacterIds(any())).thenReturn(Arrays.asList(
                new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L).setAppearanceIndex(1),
                new AiDramaCharacterAppearanceDO().setId(12L).setCharacterId(1L).setAppearanceIndex(2),
                new AiDramaCharacterAppearanceDO().setId(21L).setCharacterId(2L).setAppearanceIndex(1)));
        when(assetService.getLocationList(PROJECT_ID)).thenReturn(
                Collections.singletonList(new AiDramaLocationDO().setId(31L).setName("车厢")));
        when(assetService.getVisualAssetList(PROJECT_ID)).thenReturn(
                Collections.singletonList(new AiDramaVisualAssetDO().setId(41L).setTitle("怀表")));
        ChatModel chatModel = mockChat("```json\n[{\"sceneNo\":1,\"title\":\"进站\",\"duration\":6,\"videoPrompt\":\"列车进站\","
                + "\"dialogues\":[{\"character\":\"林夏\",\"line\":\"最后一班了\"},{\"character\":\"\",\"line\":\"夜深了\"}],"
                + "\"characters\":[\"林夏\",\"路人\"],\"location\":\"车厢\",\"props\":[\"怀表\"]},"
                + "{\"title\":\"对视\",\"characters\":[\"老周\"]}]\n```", AiPlatformEnum.DEEPSEEK);

        scriptService.generateStoryboardListMy(USER_ID, chatReqVO());

        ArgumentCaptor<List<AiDramaStoryboardDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(storyboardService).replaceStoryboardList(eq(PROJECT_ID), captor.capture());
        List<AiDramaStoryboardDO> storyboards = captor.getValue();
        assertEquals(2, storyboards.size());
        // 第 1 个镜头：角色取第 1 个形象，未知角色忽略；旁白没有角色
        AiDramaStoryboardDO first = storyboards.get(0);
        assertEquals(SCRIPT_ID, first.getScriptId());
        assertEquals(6, first.getDuration());
        assertEquals(List.of(11L), first.getAppearanceIds());
        assertEquals(31L, first.getLocationId());
        assertEquals(List.of(41L), first.getVisualAssetIds());
        assertEquals(1L, first.getDialogues().get(0).getCharacterId());
        assertNull(first.getDialogues().get(1).getCharacterId());
        // 第 2 个镜头：缺省场次取序号，缺省时长 5 秒
        AiDramaStoryboardDO second = storyboards.get(1);
        assertEquals(2, second.getSceneNo());
        assertEquals(5, second.getDuration());
        assertEquals(List.of(21L), second.getAppearanceIds());
        assertNull(second.getLocationId());
        // 输出 JSON 数组：DeepSeek 也不开启 json_object
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        assertNull(((DeepSeekChatOptions) promptCaptor.getValue().getOptions()).getResponseFormat());
    }

    // ========== 工具 ==========

    private void mockChat(String text) {
        mockChat(text, AiPlatformEnum.OPENAI);
    }

    private ChatModel mockChat(String text, AiPlatformEnum platform) {
        AiModelDO model = new AiModelDO().setId(CHAT_MODEL_ID).setType(AiModelTypeEnum.CHAT.getType())
                .setPlatform(platform.getPlatform()).setModel("gpt-4o");
        when(modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType())).thenReturn(model);
        ChatModel chatModel = mock(ChatModel.class);
        when(modelService.getChatModel(CHAT_MODEL_ID)).thenReturn(chatModel);
        ChatResponse response = mock(ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn(text);
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        return chatModel;
    }

    private void mockScript() {
        when(scriptMapper.selectByProjectId(PROJECT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID)
                .setProjectId(PROJECT_ID).setContent("第一场 车厢"));
    }

    private static AiDramaProjectDO project() {
        return new AiDramaProjectDO().setId(PROJECT_ID).setUserId(USER_ID).setName("末班地铁").setIdea("末班车上的相遇")
                .setStyle("电影感");
    }

    private static AiDramaChatReqVO chatReqVO() {
        AiDramaChatReqVO reqVO = new AiDramaChatReqVO();
        reqVO.setProjectId(PROJECT_ID);
        return reqVO;
    }

}
