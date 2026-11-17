package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatStreamReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardImportReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaScriptMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaChatTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaProjectStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStyleEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiReasoningUtils;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
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
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public void testPolishScriptMy_newVersion() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        when(scriptMapper.selectLatestByProjectId(PROJECT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID)
                .setProjectId(PROJECT_ID).setContent("旧剧本").setWorldbuilding("近未来城市"));
        ChatModel chatModel = mockChat("{\"content\":\"新剧本\"}", AiPlatformEnum.OPENAI);
        AiDramaScriptPolishReqVO reqVO = new AiDramaScriptPolishReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setNote("结尾加反转");

        scriptService.polishScriptMy(USER_ID, reqVO);

        // 断言：新增一个版本，带上修改说明，沿用世界观；原版本不修改
        ArgumentCaptor<AiDramaScriptDO> captor = ArgumentCaptor.forClass(AiDramaScriptDO.class);
        verify(scriptMapper).insert(captor.capture());
        assertEquals("新剧本", captor.getValue().getContent());
        assertEquals("结尾加反转", captor.getValue().getRevisionNote());
        assertEquals("近未来城市", captor.getValue().getWorldbuilding());
        verify(scriptMapper, never()).updateById(any(AiDramaScriptDO.class));
        // 断言：提示词带上世界观
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        assertTrue(promptCaptor.getValue().getContents().contains("近未来城市"));
        // 断言：项目置为剧本已修改
        verify(projectService).updateProjectStatus(PROJECT_ID, AiDramaProjectStatusEnum.SCRIPT_CHANGED.getStatus());
    }

    @Test
    public void testPolishScriptMy_scriptNotExists() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        AiDramaScriptPolishReqVO reqVO = new AiDramaScriptPolishReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setNote("结尾加反转");

        assertServiceException(() -> scriptService.polishScriptMy(USER_ID, reqVO), DRAMA_SCRIPT_NOT_EXISTS);
    }

    // ========== 保存剧本 ==========

    @Test
    public void testUpdateScriptMy_outlineOmitted() {
        when(scriptMapper.selectById(SCRIPT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID).setProjectId(PROJECT_ID)
                .setContent("第一场 车厢").setOutline("末班车上的相遇"));
        mockScript();
        // 准备：只传标题、正文（例如画布剧本节点），正文未变
        AiDramaScriptUpdateReqVO reqVO = new AiDramaScriptUpdateReqVO();
        reqVO.setId(SCRIPT_ID);
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setTitle("末班地铁");
        reqVO.setContent("第一场 车厢");

        assertEquals(SCRIPT_ID, scriptService.updateScriptMy(USER_ID, reqVO));

        // 断言：更新该版本；未传大纲不视为修改，项目状态不变
        verify(scriptMapper).updateById(argThat((AiDramaScriptDO script) -> SCRIPT_ID.equals(script.getId())));
        verify(scriptMapper, never()).insert(any(AiDramaScriptDO.class));
        verify(projectService, never()).updateProjectStatus(any(), any());
    }

    @Test
    public void testUpdateScriptMy_contentOmitted() {
        when(scriptMapper.selectById(SCRIPT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID).setProjectId(PROJECT_ID)
                .setContent("第一场 车厢"));
        mockScript();
        // 准备：只传标题，不传正文
        AiDramaScriptUpdateReqVO reqVO = new AiDramaScriptUpdateReqVO();
        reqVO.setId(SCRIPT_ID);
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setTitle("末班地铁");

        scriptService.updateScriptMy(USER_ID, reqVO);

        // 断言：未传正文不视为修改，项目状态不变
        verify(scriptMapper).updateById(argThat((AiDramaScriptDO script) -> script.getContent() == null));
        verify(projectService, never()).updateProjectStatus(any(), any());
    }

    @Test
    public void testUpdateScriptMy_historyVersion() {
        when(scriptMapper.selectById(SCRIPT_ID - 1)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID - 1)
                .setProjectId(PROJECT_ID).setContent("旧剧本"));
        mockScript();
        AiDramaScriptUpdateReqVO reqVO = new AiDramaScriptUpdateReqVO();
        reqVO.setId(SCRIPT_ID - 1);
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setContent("改过的旧剧本");

        scriptService.updateScriptMy(USER_ID, reqVO);

        // 断言：只更新该历史版本，不覆盖当前剧本，项目状态不变
        verify(scriptMapper).updateById(argThat((AiDramaScriptDO script) -> Long.valueOf(SCRIPT_ID - 1).equals(script.getId())));
        verify(projectService, never()).updateProjectStatus(any(), any());
    }

    @Test
    public void testUpdateScriptMy_otherProject() {
        when(scriptMapper.selectById(SCRIPT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID).setProjectId(PROJECT_ID + 1));
        AiDramaScriptUpdateReqVO reqVO = new AiDramaScriptUpdateReqVO();
        reqVO.setId(SCRIPT_ID);
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setContent("正文");

        assertServiceException(() -> scriptService.updateScriptMy(USER_ID, reqVO), DRAMA_SCRIPT_NOT_EXISTS);
        verify(scriptMapper, never()).updateById(any(AiDramaScriptDO.class));
    }

    @Test
    public void testUpdateScriptMy_createVersion() {
        // 准备：未指定编号，插入后成为当前剧本
        doAnswer(invocation -> {
            invocation.<AiDramaScriptDO>getArgument(0).setId(SCRIPT_ID + 1);
            return 1;
        }).when(scriptMapper).insert(any(AiDramaScriptDO.class));
        when(scriptMapper.selectLatestByProjectId(PROJECT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID + 1)
                .setProjectId(PROJECT_ID));
        AiDramaScriptUpdateReqVO reqVO = new AiDramaScriptUpdateReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setContent("新剧本");

        assertEquals(Long.valueOf(SCRIPT_ID + 1), scriptService.updateScriptMy(USER_ID, reqVO));

        // 断言：新增手动编写的版本，项目置为剧本已修改
        verify(scriptMapper).insert(argThat((AiDramaScriptDO script) -> "新剧本".equals(script.getContent())));
        verify(projectService).updateProjectStatus(PROJECT_ID, AiDramaProjectStatusEnum.SCRIPT_CHANGED.getStatus());
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
                new AiDramaStoryboardDO().setId(1L).setScriptId(SCRIPT_ID).setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())));

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
                new AiDramaCharacterAppearanceDO().setId(11L).setCharacterId(1L).setAppearanceIndex(0),
                new AiDramaCharacterAppearanceDO().setId(12L).setCharacterId(1L).setAppearanceIndex(1),
                new AiDramaCharacterAppearanceDO().setId(21L).setCharacterId(2L).setAppearanceIndex(0)));
        when(assetService.getLocationList(PROJECT_ID)).thenReturn(
                Collections.singletonList(new AiDramaLocationDO().setId(31L).setName("车厢")));
        when(assetService.getPropList(PROJECT_ID)).thenReturn(
                Collections.singletonList(new AiDramaVisualAssetDO().setId(41L).setTitle("怀表")));
        ChatModel chatModel = mockChat("```json\n[{\"sceneNo\":1,\"title\":\"进站\",\"duration\":6,\"videoPrompt\":\"列车进站\","
                + "\"dialogues\":[{\"character\":\"林夏\",\"line\":\"最后一班了\"},{\"character\":\"\",\"line\":\"夜深了\"}],"
                + "\"characters\":[{\"name\":\"林夏\",\"slot\":\"左侧\"},\"路人\"],\"location\":\"车厢\",\"props\":[\"怀表\"],"
                + "\"sceneNumber\":1,\"startState\":\"林夏站在车门内侧\",\"presentCharacters\":[\"林夏\",\"林夏\"],"
                + "\"shotDesign\":{\"focus\":\"林夏\"},\"performanceBeats\":[{\"name\":\"林夏\",\"acting\":\"抬头看站牌\"}]},"
                + "{\"sceneNo\":1,\"title\":\"对视\",\"characters\":[\"老周\"]}]\n```", AiPlatformEnum.DEEPSEEK);

        scriptService.generateStoryboardListMy(USER_ID, chatReqVO());

        ArgumentCaptor<List<AiDramaStoryboardDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(storyboardService).replaceStoryboardList(eq(PROJECT_ID), eq(SCRIPT_ID), captor.capture());
        verify(projectService).updateProjectStatus(PROJECT_ID, AiDramaProjectStatusEnum.STORYBOARD_READY.getStatus());
        List<AiDramaStoryboardDO> storyboards = captor.getValue();
        assertEquals(2, storyboards.size());
        // 第 1 个镜头：角色取第 1 个形象并带上站位，未知角色忽略；旁白没有角色；返回了道具时明确指定
        AiDramaStoryboardDO first = storyboards.get(0);
        assertEquals(SCRIPT_ID, first.getScriptId());
        assertEquals(6, first.getDuration());
        assertEquals(1, first.getCharacters().size());
        assertEquals(11L, first.getCharacters().get(0).getAppearanceId());
        assertEquals("左侧", first.getCharacters().get(0).getSlot());
        assertEquals(31L, first.getLocationId());
        assertEquals(List.of(41L), first.getVisualAssetIds());
        assertEquals(1L, first.getDialogues().get(0).getCharacterId());
        assertNull(first.getDialogues().get(1).getCharacterId());
        // 第 1 个镜头的规划字段：原文场次、起始状态、去重的出镜角色、拍法设计与表演节拍
        assertEquals(1, first.getSceneNo());
        assertEquals(1, first.getContinuity().getSceneNumber());
        assertEquals("林夏站在车门内侧", first.getContinuity().getStartState());
        assertEquals(List.of("林夏"), first.getContinuity().getPresentCharacters());
        assertEquals("林夏", first.getContinuity().getShotDesign().get("focus"));
        assertEquals("抬头看站牌", first.getContinuity().getPerformanceBeats().get(0).getActing());
        // 第 2 个镜头：序号按顺序编号（忽略模型返回的重复序号），缺省时长 5 秒；未返回道具时为空，按道具关联的镜头序号匹配
        AiDramaStoryboardDO second = storyboards.get(1);
        assertEquals(2, second.getSceneNo());
        assertNull(second.getContinuity().getPresentCharacters());
        assertEquals(5, second.getDuration());
        assertEquals(21L, second.getCharacters().get(0).getAppearanceId());
        assertNull(second.getLocationId());
        assertNull(second.getVisualAssetIds());
        // 输出 JSON 数组：DeepSeek 也不开启 json_object
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        assertNull(((DeepSeekChatOptions) promptCaptor.getValue().getOptions()).getResponseFormat());
    }

    // ========== 摄影规则、表演指导、关键帧规划 ==========

    @Test
    @SuppressWarnings("unchecked")
    public void testGenerateStoryboardDetailMy_actingNotes() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        // 准备：当前剧本 2 个镜头，另有其它剧本版本的镜头
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaStoryboardDO().setId(1L).setScriptId(SCRIPT_ID).setSceneNo(1),
                new AiDramaStoryboardDO().setId(9L).setScriptId(999L).setSceneNo(1),
                new AiDramaStoryboardDO().setId(2L).setScriptId(SCRIPT_ID).setSceneNo(2)));
        mockChat("[{\"panelNumber\":2,\"characters\":[]},"
                + "{\"panelNumber\":1,\"characters\":[{\"name\":\"林夏\",\"acting\":\"攥紧车票，目光躲闪\"},{\"name\":\"老周\"}]}]");

        scriptService.generateStoryboardDetailMy(USER_ID, chatReqVO(), AiDramaChatTypeEnum.ACTING_NOTES);

        // 断言：按镜头序号对应当前剧本的分镜；缺少表演的角色忽略
        ArgumentCaptor<List<AiDramaStoryboardDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(storyboardService).updateStoryboardList(captor.capture());
        List<AiDramaStoryboardDO> updateList = captor.getValue();
        assertEquals(2, updateList.size());
        assertEquals(1L, updateList.get(0).getId());
        assertEquals(1, updateList.get(0).getActingNotes().size());
        assertEquals("林夏", updateList.get(0).getActingNotes().get(0).getName());
        assertEquals("攥紧车票，目光躲闪", updateList.get(0).getActingNotes().get(0).getActing());
        assertEquals(2L, updateList.get(1).getId());
        assertTrue(updateList.get(1).getActingNotes().isEmpty());
    }

    @Test
    public void testGenerateStoryboardDetailMy_framePlan() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Collections.singletonList(
                new AiDramaStoryboardDO().setId(1L).setScriptId(SCRIPT_ID).setSceneNo(1)
                        .setContinuity(new AiDramaStoryboardDO.Continuity().setKeyframeDirective("人物居中"))));
        mockChat("[{\"panelNumber\":1,\"imagePrompt\":\"林夏站在车门边\",\"frameFocus\":\"车票\"}]");

        scriptService.generateStoryboardDetailMy(USER_ID, chatReqVO(), AiDramaChatTypeEnum.FRAME_PLAN);

        // 断言：写入关键帧画面描述、画面焦点，保留原有的连续性设置
        verify(storyboardService).updateStoryboardList(argThat(list -> {
            AiDramaStoryboardDO updateObj = list.get(0);
            return "林夏站在车门边".equals(updateObj.getImagePrompt())
                    && "车票".equals(updateObj.getContinuity().getFrameFocus())
                    && "人物居中".equals(updateObj.getContinuity().getKeyframeDirective());
        }));
    }

    @Test
    public void testGenerateStoryboardDetailMy_missingPanel() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        when(storyboardService.getStoryboardList(PROJECT_ID)).thenReturn(Arrays.asList(
                new AiDramaStoryboardDO().setId(1L).setScriptId(SCRIPT_ID).setSceneNo(1),
                new AiDramaStoryboardDO().setId(2L).setScriptId(SCRIPT_ID).setSceneNo(2)));
        mockChat("[{\"panelNumber\":1,\"colorTone\":\"冷蓝\"}]");

        assertServiceException(() -> scriptService.generateStoryboardDetailMy(USER_ID, chatReqVO(),
                AiDramaChatTypeEnum.PHOTOGRAPHY_RULES), DRAMA_CHAT_FAIL, "生成摄影规则", "模型没有返回第 2 个镜头");
        verify(storyboardService, never()).updateStoryboardList(any());
    }

    @Test
    public void testGenerateStoryboardDetailMy_storyboardEmpty() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();

        assertServiceException(() -> scriptService.generateStoryboardDetailMy(USER_ID, chatReqVO(),
                AiDramaChatTypeEnum.PHOTOGRAPHY_RULES), DRAMA_STORYBOARD_EMPTY);
    }

    // ========== 规划、导入分镜 ==========

    @Test
    @SuppressWarnings("unchecked")
    public void testImportStoryboardListMy_success() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        AiDramaStoryboardSaveReqVO item = new AiDramaStoryboardSaveReqVO();
        item.setId(99L);
        item.setTitle("进站");
        AiDramaStoryboardImportReqVO reqVO = new AiDramaStoryboardImportReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setScriptId(SCRIPT_ID);
        reqVO.setStoryboards(List.of(item));

        scriptService.importStoryboardListMy(USER_ID, reqVO);

        // 断言：替换当前剧本的分镜，不沿用传入的编号，缺省场次按顺序；项目置为分镜就绪
        ArgumentCaptor<List<AiDramaStoryboardDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(storyboardService).replaceStoryboardList(eq(PROJECT_ID), eq(SCRIPT_ID), captor.capture());
        assertNull(captor.getValue().get(0).getId());
        assertEquals(PROJECT_ID, captor.getValue().get(0).getProjectId());
        assertEquals(1, captor.getValue().get(0).getSceneNo());
        verify(projectService).updateProjectStatus(PROJECT_ID, AiDramaProjectStatusEnum.STORYBOARD_READY.getStatus());
    }

    @Test
    public void testImportStoryboardListMy_sceneNoDuplicate() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        // 准备：第 2 个未填序号，按顺序编号为 2，与第 1 个填写的 2 重复
        AiDramaStoryboardSaveReqVO first = new AiDramaStoryboardSaveReqVO();
        first.setSceneNo(2);
        AiDramaStoryboardImportReqVO reqVO = new AiDramaStoryboardImportReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setScriptId(SCRIPT_ID);
        reqVO.setStoryboards(List.of(first, new AiDramaStoryboardSaveReqVO()));

        assertThrows(ServiceException.class, () -> scriptService.importStoryboardListMy(USER_ID, reqVO));
        verify(storyboardService, never()).replaceStoryboardList(any(), any(), any());
    }

    @Test
    public void testImportStoryboardListMy_scriptChanged() {
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        mockScript();
        AiDramaStoryboardImportReqVO reqVO = new AiDramaStoryboardImportReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setScriptId(SCRIPT_ID - 1);
        reqVO.setStoryboards(List.of(new AiDramaStoryboardSaveReqVO()));

        assertThrows(ServiceException.class, () -> scriptService.importStoryboardListMy(USER_ID, reqVO));
        verify(storyboardService, never()).replaceStoryboardList(any(), any(), any());
    }

    // ========== 思考档位 ==========

    @Test
    public void testGenerateScriptMy_reasoningHttpRoute() {
        // 准备参数：DeepSeek 配置默认档位 off
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        Map<String, Object> options = Map.of("thinking", Map.of("type", "disabled"));
        mockChat("{\"content\":\"第一场 车厢\"}", chatModelDO(AiPlatformEnum.DEEPSEEK, "deepseek-v4-flash")
                .setReasoningConfig(offReasoningConfig(options)));

        // 调用
        scriptService.generateScriptMy(USER_ID, chatReqVO());

        // 断言：默认档位的思考参数传给客户端（HTTP 层补丁）
        verify(modelService).getChatModel(CHAT_MODEL_ID, options);
    }

    @Test
    public void testStreamChatMy_reasoningAlibaba() {
        // 准备参数：通义配置默认档位 off
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        Map<String, Object> options = Map.of(AiReasoningUtils.ALIBABA_ENABLE_THINKING, false);
        AiModelDO model = chatModelDO(AiPlatformEnum.ALIBABA, "qwen-plus").setReasoningConfig(offReasoningConfig(options));
        when(modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType())).thenReturn(model);
        ChatModel chatModel = mock(ChatModel.class);
        when(modelService.getChatModel(eq(CHAT_MODEL_ID), any())).thenReturn(chatModel);
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.never());
        AiDramaChatStreamReqVO reqVO = new AiDramaChatStreamReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setType(AiDramaChatTypeEnum.SCRIPT_GENERATE.getType());

        // 调用
        scriptService.streamChatMy(USER_ID, reqVO);

        // 断言：客户端与 ChatOptions 使用同一份思考参数，enable_thinking 映射到原生字段
        verify(modelService).getChatModel(CHAT_MODEL_ID, options);
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).stream(captor.capture());
        assertEquals(Boolean.FALSE, ((DashScopeChatOptions) captor.getValue().getOptions()).getEnableThinking());
    }

    @Test
    public void testGenerateScriptMy_reasoningNotConfigured() {
        // 准备参数：通义未配置思考
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenReturn(project());
        ChatModel chatModel = mockChat("{\"content\":\"第一场 车厢\"}", chatModelDO(AiPlatformEnum.ALIBABA, "qwen-plus"));

        // 调用
        scriptService.generateScriptMy(USER_ID, chatReqVO());

        // 断言：不带思考参数，保持原有行为
        verify(modelService).getChatModel(CHAT_MODEL_ID, null);
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        assertEquals(Boolean.TRUE, ((DashScopeChatOptions) captor.getValue().getOptions()).getEnableThinking());
    }

    // ========== 工具 ==========

    private void mockChat(String text) {
        mockChat(text, AiPlatformEnum.OPENAI);
    }

    private ChatModel mockChat(String text, AiPlatformEnum platform) {
        return mockChat(text, chatModelDO(platform, "gpt-4o"));
    }

    private ChatModel mockChat(String text, AiModelDO model) {
        when(modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType())).thenReturn(model);
        ChatModel chatModel = mock(ChatModel.class);
        when(modelService.getChatModel(eq(CHAT_MODEL_ID), any())).thenReturn(chatModel);
        ChatResponse response = mock(ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getText()).thenReturn(text);
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        return chatModel;
    }

    private static AiModelDO chatModelDO(AiPlatformEnum platform, String model) {
        return new AiModelDO().setId(CHAT_MODEL_ID).setType(AiModelTypeEnum.CHAT.getType())
                .setPlatform(platform.getPlatform()).setModel(model);
    }

    private static AiModelDO.ReasoningConfig offReasoningConfig(Map<String, Object> options) {
        return AiModelDO.ReasoningConfig.builder().defaultLevel(AiReasoningUtils.REASONING_LEVEL_OFF)
                .levels(List.of(AiModelDO.ReasoningLevel.builder().level(AiReasoningUtils.REASONING_LEVEL_OFF)
                        .options(options).build()))
                .build();
    }

    private void mockScript() {
        when(scriptMapper.selectLatestByProjectId(PROJECT_ID)).thenReturn(new AiDramaScriptDO().setId(SCRIPT_ID)
                .setProjectId(PROJECT_ID).setContent("第一场 车厢"));
    }

    private static AiDramaProjectDO project() {
        return new AiDramaProjectDO().setId(PROJECT_ID).setUserId(USER_ID).setName("末班地铁").setIdea("末班车上的相遇")
                .setStyle(AiDramaStyleEnum.REALISTIC.getStyle());
    }

    private static AiDramaChatReqVO chatReqVO() {
        AiDramaChatReqVO reqVO = new AiDramaChatReqVO();
        reqVO.setProjectId(PROJECT_ID);
        return reqVO;
    }

}
