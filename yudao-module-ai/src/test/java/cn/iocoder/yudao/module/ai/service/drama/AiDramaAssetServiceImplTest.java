package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.AiDramaAppearanceSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.AiDramaImageGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.*;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiDramaAssetServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiDramaAssetServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long CHARACTER_ID = 200L;
    private static final Long APPEARANCE_ID = 300L;
    private static final Long IMAGE_MODEL_ID = 10L;

    @Spy
    @InjectMocks
    private AiDramaAssetServiceImpl assetService;

    @Mock
    private AiDramaCharacterMapper characterMapper;
    @Mock
    private AiDramaCharacterAppearanceMapper appearanceMapper;
    @Mock
    private AiDramaLocationMapper locationMapper;
    @Mock
    private AiDramaVisualAssetMapper visualAssetMapper;
    @Mock
    private AiDramaAudioMapper audioMapper;
    @Mock
    private AiDramaProjectService projectService;
    @Mock
    private AiModelService modelService;
    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private FileApi fileApi;

    // ========== 候选图 ==========

    @Test
    public void testAppendPlaceholder_removeFailed() {
        List<AiDramaCharacterAppearanceDO.Candidate> candidates = Arrays.asList(
                new AiDramaCharacterAppearanceDO.Candidate().setUrl("https://files.example.com/1.png"),
                new AiDramaCharacterAppearanceDO.Candidate().setModelId(9L));

        List<AiDramaCharacterAppearanceDO.Candidate> result = AiDramaAssetServiceImpl.appendPlaceholder(candidates,
                "提示词", IMAGE_MODEL_ID);

        assertEquals(2, result.size());
        assertEquals("https://files.example.com/1.png", result.get(0).getUrl());
        assertNull(result.get(1).getUrl());
        assertEquals(IMAGE_MODEL_ID, result.get(1).getModelId());
        assertEquals("提示词", result.get(1).getDescription());
    }

    @Test
    public void testBuildImagePrompt() {
        assertEquals("林夏，短发，画风：电影感，人物设定图",
                AiDramaAssetServiceImpl.buildImagePrompt("电影感", "人物设定图", "林夏", " 短发 ", null, " "));
    }

    @Test
    public void testUpdateAppearanceMy_selectedIndexInvalid() {
        mockAppearanceOwner(appearance().setCandidates(Collections.singletonList(
                new AiDramaCharacterAppearanceDO.Candidate().setModelId(IMAGE_MODEL_ID))));
        AiDramaAppearanceSaveReqVO reqVO = new AiDramaAppearanceSaveReqVO();
        reqVO.setId(APPEARANCE_ID);
        reqVO.setSelectedIndex(0);

        // 生成中、已失败的占位候选不能选用
        assertServiceException(() -> assetService.updateAppearanceMy(USER_ID, reqVO), DRAMA_SELECTED_INDEX_INVALID);
    }

    @Test
    public void testUpdateAppearanceMy_otherUser() {
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenReturn(appearance());
        when(characterMapper.selectById(CHARACTER_ID)).thenReturn(character());
        when(projectService.validateProjectMy(PROJECT_ID, USER_ID)).thenThrow(exception(DRAMA_PROJECT_NOT_EXISTS));
        AiDramaAppearanceSaveReqVO reqVO = new AiDramaAppearanceSaveReqVO();
        reqVO.setId(APPEARANCE_ID);

        assertServiceException(() -> assetService.updateAppearanceMy(USER_ID, reqVO), DRAMA_PROJECT_NOT_EXISTS);
        verify(appearanceMapper, never()).updateById(any(AiDramaCharacterAppearanceDO.class));
    }

    // ========== 出图 ==========

    @Test
    public void testGenerateAppearanceImageMy_midjourney() {
        mockAppearanceOwner(appearance());
        when(modelService.validateModel(IMAGE_MODEL_ID)).thenReturn(imageModel(AiPlatformEnum.MIDJOURNEY.getPlatform()));

        assertServiceException(() -> assetService.generateAppearanceImageMy(USER_ID, imageReqVO()),
                DRAMA_IMAGE_MODEL_ERROR, "绘画模型（Midjourney 暂不支持）");
    }

    @Test
    public void testGenerateAppearanceImageMy_running() {
        mockAppearanceOwner(appearance().setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));
        when(modelService.validateModel(IMAGE_MODEL_ID)).thenReturn(imageModel(AiPlatformEnum.OPENAI.getPlatform()));

        assertServiceException(() -> assetService.generateAppearanceImageMy(USER_ID, imageReqVO()), DRAMA_TASK_RUNNING);
    }

    @Test
    public void testGenerateAppearanceImageMy_success() {
        mockAppearanceOwner(appearance());
        when(modelService.validateModel(IMAGE_MODEL_ID)).thenReturn(imageModel(AiPlatformEnum.OPENAI.getPlatform()));
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(new AiDramaProjectDO().setId(PROJECT_ID).setStyle("电影感"));
        doNothing().when(assetService).executeImage(any(), any(), any(), any(), any(), any());

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(assetService);
            assetService.generateAppearanceImageMy(USER_ID, imageReqVO());
        }

        // 断言：追加占位候选，置为生成中
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).updateById(captor.capture());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getCandidates().size());
        assertEquals(IMAGE_MODEL_ID, captor.getValue().getCandidates().get(0).getModelId());
        // 断言：异步出图，带上记录的任务编号；参考图取角色的参考图
        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(assetService).executeImage(eq(AiDramaTaskTypeEnum.APPEARANCE), eq(APPEARANCE_ID), taskIdCaptor.capture(),
                any(), contains("林夏"), eq(Collections.singletonList("https://files.example.com/ref.png")));
        assertNotNull(taskIdCaptor.getValue());
        assertEquals(taskIdCaptor.getValue(), captor.getValue().getTaskId());
    }

    @Test
    public void testExecuteImage_appearanceSuccess() {
        ImageModel imageModel = mock(ImageModel.class);
        ImageResponse response = mock(ImageResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getB64Json()).thenReturn(null);
        when(response.getResult().getOutput().getUrl()).thenReturn("https://provider.example.com/tmp.png");
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(response);
        when(fileApi.createFile(any(byte[].class))).thenReturn("https://files.example.com/new.png");
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenReturn(appearance().setCandidates(new ArrayList<>(List.of(
                new AiDramaCharacterAppearanceDO.Candidate().setModelId(IMAGE_MODEL_ID)))));
        AiDramaAssetServiceImpl.ImageClient client = new AiDramaAssetServiceImpl.ImageClient(
                imageModel(AiPlatformEnum.OPENAI.getPlatform()), null, imageModel);

        try (MockedStatic<HttpUtil> httpUtil = mockStatic(HttpUtil.class)) {
            httpUtil.when(() -> HttpUtil.downloadBytes("https://provider.example.com/tmp.png")).thenReturn(new byte[]{1});
            assetService.executeImage(AiDramaTaskTypeEnum.APPEARANCE, APPEARANCE_ID, "task-1", client, "提示词",
                    Collections.emptyList());
        }

        // 断言：回填占位候选，默认选用；以生成中、本次任务编号为条件
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).updateByIdAndStatusAndTaskId(eq(APPEARANCE_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), captor.getValue().getStatus());
        assertEquals("https://files.example.com/new.png", captor.getValue().getCandidates().get(0).getUrl());
        assertEquals(0, captor.getValue().getSelectedIndex());
    }

    @Test
    public void testExecuteImage_appearanceResubmittedDuringUpload() {
        ImageModel imageModel = mock(ImageModel.class);
        ImageResponse response = mock(ImageResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(response.getResult().getOutput().getB64Json()).thenReturn(null);
        when(response.getResult().getOutput().getUrl()).thenReturn("https://provider.example.com/tmp.png");
        when(imageModel.call(any(ImagePrompt.class))).thenReturn(response);
        // 准备：库中的记录，旧任务（task-old）生成中；每次读取返回新对象，避免回填修改到库中的数据
        AtomicReference<Integer> dbStatus = new AtomicReference<>(AiDramaStatusEnum.IN_PROGRESS.getStatus());
        AtomicReference<String> dbTaskId = new AtomicReference<>("task-old");
        List<AiDramaCharacterAppearanceDO> written = new ArrayList<>();
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenAnswer(invocation -> appearance()
                .setStatus(dbStatus.get()).setTaskId(dbTaskId.get()).setCandidates(new ArrayList<>(List.of(
                        new AiDramaCharacterAppearanceDO.Candidate().setModelId(IMAGE_MODEL_ID)))));
        when(appearanceMapper.updateByIdAndStatusAndTaskId(eq(APPEARANCE_ID), any(), any(), any())).thenAnswer(invocation -> {
            if (!Objects.equals(invocation.getArgument(1), dbStatus.get())
                    || !Objects.equals(invocation.getArgument(2), dbTaskId.get())) {
                return 0;
            }
            AiDramaCharacterAppearanceDO updateObj = invocation.getArgument(3);
            written.add(updateObj);
            dbStatus.set(updateObj.getStatus());
            return 1;
        });
        // 准备：旧任务上传期间，被管理员标记失败并重新提交，任务编号换成 task-new
        when(fileApi.createFile(any(byte[].class))).thenAnswer(invocation -> {
            if (Objects.equals(dbTaskId.get(), "task-old")) {
                dbStatus.set(AiDramaStatusEnum.FAIL.getStatus());
                dbStatus.set(AiDramaStatusEnum.IN_PROGRESS.getStatus());
                dbTaskId.set("task-new");
                return "https://files.example.com/old.png";
            }
            return "https://files.example.com/new.png";
        });
        AiDramaAssetServiceImpl.ImageClient client = new AiDramaAssetServiceImpl.ImageClient(
                imageModel(AiPlatformEnum.OPENAI.getPlatform()), null, imageModel);

        try (MockedStatic<HttpUtil> httpUtil = mockStatic(HttpUtil.class)) {
            httpUtil.when(() -> HttpUtil.downloadBytes("https://provider.example.com/tmp.png")).thenReturn(new byte[]{1});
            // 调用：旧任务上传完成后回写，不命中，新任务仍生成中
            assetService.executeImage(AiDramaTaskTypeEnum.APPEARANCE, APPEARANCE_ID, "task-old", client, "提示词",
                    Collections.emptyList());
            assertTrue(written.isEmpty());
            assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), dbStatus.get());
            assertEquals("task-new", dbTaskId.get());
            // 调用：新任务结束，回填新任务的占位
            assetService.executeImage(AiDramaTaskTypeEnum.APPEARANCE, APPEARANCE_ID, "task-new", client, "提示词",
                    Collections.emptyList());
        }

        assertEquals(1, written.size());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), written.get(0).getStatus());
        assertEquals(1, written.get(0).getCandidates().size());
        assertEquals("https://files.example.com/new.png", written.get(0).getCandidates().get(0).getUrl());
    }

    @Test
    public void testExecuteImage_visualAssetFail() {
        ImageModel imageModel = mock(ImageModel.class);
        when(imageModel.call(any(ImagePrompt.class))).thenThrow(new IllegalStateException("内容审核未通过"));
        AiDramaAssetServiceImpl.ImageClient client = new AiDramaAssetServiceImpl.ImageClient(
                imageModel(AiPlatformEnum.OPENAI.getPlatform()), null, imageModel);

        assetService.executeImage(AiDramaTaskTypeEnum.VISUAL_ASSET, 5L, "task-1", client, "提示词", Collections.emptyList());

        ArgumentCaptor<AiDramaVisualAssetDO> captor = ArgumentCaptor.forClass(AiDramaVisualAssetDO.class);
        verify(visualAssetMapper).updateByIdAndStatusAndTaskId(eq(5L), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getStatus());
        assertEquals("内容审核未通过", captor.getValue().getErrorMessage());
        assertNull(captor.getValue().getImageUrl());
    }

    @Test
    public void testExecuteImage_visualAssetResubmittedDuringDraw() {
        // 准备：库中的记录，旧任务（task-old）生成中
        AtomicReference<Integer> dbStatus = new AtomicReference<>(AiDramaStatusEnum.IN_PROGRESS.getStatus());
        AtomicReference<String> dbTaskId = new AtomicReference<>("task-old");
        List<AiDramaVisualAssetDO> written = new ArrayList<>();
        when(visualAssetMapper.updateByIdAndStatusAndTaskId(eq(5L), any(), any(), any())).thenAnswer(invocation -> {
            if (!Objects.equals(invocation.getArgument(1), dbStatus.get())
                    || !Objects.equals(invocation.getArgument(2), dbTaskId.get())) {
                return 0;
            }
            AiDramaVisualAssetDO updateObj = invocation.getArgument(3);
            written.add(updateObj);
            dbStatus.set(updateObj.getStatus());
            return 1;
        });
        // 准备：旧任务出图期间，被管理员标记失败并重新提交，任务编号换成 task-new；出图失败
        ImageModel imageModel = mock(ImageModel.class);
        when(imageModel.call(any(ImagePrompt.class))).thenAnswer(invocation -> {
            if (Objects.equals(dbTaskId.get(), "task-old")) {
                dbStatus.set(AiDramaStatusEnum.FAIL.getStatus());
                dbStatus.set(AiDramaStatusEnum.IN_PROGRESS.getStatus());
                dbTaskId.set("task-new");
            }
            throw new IllegalStateException("出图超时");
        });
        AiDramaAssetServiceImpl.ImageClient client = new AiDramaAssetServiceImpl.ImageClient(
                imageModel(AiPlatformEnum.OPENAI.getPlatform()), null, imageModel);

        // 调用：旧任务的失败不结束新任务
        assetService.executeImage(AiDramaTaskTypeEnum.VISUAL_ASSET, 5L, "task-old", client, "提示词", Collections.emptyList());
        assertTrue(written.isEmpty());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), dbStatus.get());
        // 调用：新任务自身的结果正常写入
        assetService.executeImage(AiDramaTaskTypeEnum.VISUAL_ASSET, 5L, "task-new", client, "提示词", Collections.emptyList());
        assertEquals(1, written.size());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), written.get(0).getStatus());
    }

    @Test
    public void testResubmitAppearanceImage_notFail() {
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenReturn(appearance().setStatus(AiDramaStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> assetService.resubmitAppearanceImage(APPEARANCE_ID), DRAMA_TASK_NOT_FAIL);
    }

    // ========== 角色 ==========

    @Test
    public void testCreateCharacter_firstAppearance() {
        doAnswer(invocation -> {
            ((AiDramaCharacterDO) invocation.getArgument(0)).setId(CHARACTER_ID);
            return 1;
        }).when(characterMapper).insert(any(AiDramaCharacterDO.class));

        assertEquals(CHARACTER_ID, assetService.createCharacter(new AiDramaCharacterDO().setProjectId(PROJECT_ID).setName("老周")));

        // 断言：同时创建第 1 个形象
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).insert(captor.capture());
        assertEquals(CHARACTER_ID, captor.getValue().getCharacterId());
        assertEquals(1, captor.getValue().getAppearanceIndex());
    }

    // ========== 生成任务 ==========

    @Test
    public void testGetAppearanceTaskPage_projectWithoutCharacter() {
        when(characterMapper.selectListByProjectIds(Collections.singleton(PROJECT_ID))).thenReturn(Collections.emptyList());

        assertEquals(0L, assetService.getAppearanceTaskPage(new AiDramaTaskPageReqVO(), new AiDramaTaskPageReqVO(),
                Collections.singleton(PROJECT_ID)).getTotal());
        verify(appearanceMapper, never()).selectTaskPage(any(), any(), any());
    }

    @Test
    public void testUpdateAppearanceFail_notRunning() {
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenReturn(appearance().setStatus(AiDramaStatusEnum.SUCCESS.getStatus()));

        assertServiceException(() -> assetService.updateAppearanceFail(APPEARANCE_ID, "管理员标记失败"), DRAMA_TASK_NOT_RUNNING);
        verify(appearanceMapper, never()).updateByIdAndStatusAndTaskId(any(), any(), any(), any());
    }

    @Test
    public void testUpdateAppearanceFail_success() {
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenReturn(appearance()
                .setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()).setTaskId("task-1"));

        assetService.updateAppearanceFail(APPEARANCE_ID, "管理员标记失败");

        // 断言：以读取到的状态、任务编号为条件
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).updateByIdAndStatusAndTaskId(eq(APPEARANCE_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getStatus());
        assertEquals("管理员标记失败", captor.getValue().getErrorMessage());
    }

    @Test
    public void testTimeoutImage() {
        Integer inProgress = AiDramaStatusEnum.IN_PROGRESS.getStatus();
        when(appearanceMapper.selectListByStatusAndUpdateTimeLt(eq(inProgress), any())).thenReturn(List.of(
                appearance().setStatus(inProgress).setTaskId("task-1")));
        when(appearanceMapper.updateByIdAndStatusAndTaskId(eq(APPEARANCE_ID), eq(inProgress), eq("task-1"), any())).thenReturn(1);
        when(visualAssetMapper.selectListByStatusAndUpdateTimeLt(eq(inProgress), any())).thenReturn(List.of(
                new AiDramaVisualAssetDO().setId(400L).setStatus(inProgress).setTaskId("task-2")));
        // 已被重新提交：任务编号已变，不命中
        when(visualAssetMapper.updateByIdAndStatusAndTaskId(eq(400L), eq(inProgress), eq("task-2"), any())).thenReturn(0);

        assertEquals(1, assetService.timeoutImage(LocalDateTime.now(), "任务超时"));

        // 断言：以查询到的状态、任务编号为条件
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).updateByIdAndStatusAndTaskId(eq(APPEARANCE_ID), eq(inProgress), eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.FAIL.getStatus(), captor.getValue().getStatus());
        assertEquals("任务超时", captor.getValue().getErrorMessage());
    }

    @Test
    public void testDeleteAssetListByProjectId() {
        when(characterMapper.selectListByProjectId(PROJECT_ID)).thenReturn(Collections.singletonList(character()));

        assetService.deleteAssetListByProjectId(PROJECT_ID);

        verify(appearanceMapper).deleteByCharacterIds(List.of(CHARACTER_ID));
        verify(characterMapper).deleteByProjectId(PROJECT_ID);
        verify(locationMapper).deleteByProjectId(PROJECT_ID);
        verify(visualAssetMapper).deleteByProjectId(PROJECT_ID);
        verify(audioMapper).deleteByProjectId(PROJECT_ID);
    }

    // ========== 音频 ==========

    @Test
    public void testCreateAudioMy_selectedTypeNotAllowed() {
        AiDramaAudioCreateReqVO reqVO = new AiDramaAudioCreateReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setCharacterId(CHARACTER_ID);
        reqVO.setType(AiDramaAudioTypeEnum.SAMPLE_SELECTED.getType());
        reqVO.setFileUrl("https://files.example.com/a.mp3");

        assertServiceException(() -> assetService.createAudioMy(USER_ID, reqVO),
                DRAMA_AUDIO_TYPE_ERROR, AiDramaAudioTypeEnum.SAMPLE_SELECTED.getType());
    }

    @Test
    public void testCreateAudioMy_characterOfOtherProject() {
        when(characterMapper.selectById(CHARACTER_ID)).thenReturn(character().setProjectId(999L));
        AiDramaAudioCreateReqVO reqVO = new AiDramaAudioCreateReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setCharacterId(CHARACTER_ID);
        reqVO.setType(AiDramaAudioTypeEnum.SAMPLE.getType());
        reqVO.setFileUrl("https://files.example.com/a.mp3");

        assertServiceException(() -> assetService.createAudioMy(USER_ID, reqVO), DRAMA_CHARACTER_NOT_EXISTS);
    }

    @Test
    public void testCreateAudioMy_narrationIgnoreCharacter() {
        AiDramaAudioCreateReqVO reqVO = new AiDramaAudioCreateReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setCharacterId(CHARACTER_ID);
        reqVO.setType(AiDramaAudioTypeEnum.NARRATION.getType());
        reqVO.setFileUrl("https://files.example.com/a.mp3");

        assetService.createAudioMy(USER_ID, reqVO);

        ArgumentCaptor<AiDramaAudioDO> captor = ArgumentCaptor.forClass(AiDramaAudioDO.class);
        verify(audioMapper).insert(captor.capture());
        assertNull(captor.getValue().getCharacterId());
        assertEquals(AiDramaAudioTypeEnum.NARRATION.getType(), captor.getValue().getType());
    }

    @Test
    public void testGenerateAudioMy_defaultFormat() {
        // 通义 Qwen3-TTS 只支持 wav：按模型的默认格式合成、转存
        AiModelDO model = new AiModelDO().setId(20L).setType(AiModelTypeEnum.VOICE.getType())
                .setPlatform(AiPlatformEnum.ALIBABA.getPlatform()).setModel("qwen3-tts-flash").setKeyId(30L);
        when(modelService.validateModel(20L)).thenReturn(model);
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(30L).setPlatform(AiPlatformEnum.ALIBABA.getPlatform());
        when(apiKeyService.validateApiKey(30L)).thenReturn(apiKey);
        AbstractAiSpeechModel speechModel = mock(AbstractAiSpeechModel.class);
        when(modelService.getTextToSpeechModel(apiKey, "qwen3-tts-flash")).thenReturn(speechModel);
        when(speechModel.getDefaultFormat()).thenReturn("wav");
        byte[] content = new byte[]{1};
        when(speechModel.synthesize(eq("旁白"), eq("Cherry"), eq("wav"), isNull(), any())).thenReturn(content);
        // 文件名按内容摘要：不同音频不共用同一地址
        when(fileApi.createFile(same(content), eq(DigestUtil.sha256Hex(content) + ".wav"))).thenReturn("https://files.example.com/a.wav");
        AiDramaAudioGenerateReqVO reqVO = new AiDramaAudioGenerateReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setModelId(20L);
        reqVO.setVoice("Cherry");
        reqVO.setText("旁白");

        assetService.generateAudioMy(USER_ID, reqVO);

        verify(speechModel).validateOptions(eq("旁白"), eq("Cherry"), eq("wav"), isNull());
        ArgumentCaptor<AiDramaAudioDO> captor = ArgumentCaptor.forClass(AiDramaAudioDO.class);
        verify(audioMapper).insert(captor.capture());
        assertEquals("https://files.example.com/a.wav", captor.getValue().getFileUrl());
        assertEquals(AiDramaAudioTypeEnum.NARRATION.getType(), captor.getValue().getType());
    }

    @Test
    public void testGenerateAudioMy_timeout() {
        when(characterMapper.selectById(CHARACTER_ID)).thenReturn(character());
        AiModelDO model = new AiModelDO().setId(20L).setType(AiModelTypeEnum.VOICE.getType())
                .setPlatform(AiPlatformEnum.MINIMAX.getPlatform()).setModel("speech-02-hd").setKeyId(30L);
        when(modelService.validateModel(20L)).thenReturn(model);
        AiApiKeyDO apiKey = new AiApiKeyDO().setId(30L).setPlatform(AiPlatformEnum.MINIMAX.getPlatform());
        when(apiKeyService.validateApiKey(30L)).thenReturn(apiKey);
        AiSpeechModel speechModel = mock(AiSpeechModel.class);
        when(modelService.getTextToSpeechModel(apiKey, "speech-02-hd")).thenReturn(speechModel);
        when(speechModel.synthesize(eq("你好"), eq("female"), eq("mp3"), isNull(), any())).thenReturn(null);
        AiDramaAudioGenerateReqVO reqVO = new AiDramaAudioGenerateReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setCharacterId(CHARACTER_ID);
        reqVO.setModelId(20L);
        reqVO.setVoice("female");
        reqVO.setText("你好");

        assertServiceException(() -> assetService.generateAudioMy(USER_ID, reqVO),
                DRAMA_SPEECH_FAIL, "语音模型未在时限内返回音频，请换用其它语音模型");
        verify(audioMapper, never()).insert(any(AiDramaAudioDO.class));
    }

    @Test
    public void testSelectAudioMy_success() {
        when(audioMapper.selectById(9L)).thenReturn(new AiDramaAudioDO().setId(9L).setProjectId(PROJECT_ID)
                .setCharacterId(CHARACTER_ID).setType(AiDramaAudioTypeEnum.SAMPLE.getType()));

        assetService.selectAudioMy(USER_ID, 9L);

        // 断言：原来选用的改回普通样音
        ArgumentCaptor<AiDramaAudioDO> typeCaptor = ArgumentCaptor.forClass(AiDramaAudioDO.class);
        verify(audioMapper).updateByCharacterIdAndType(eq(CHARACTER_ID),
                eq(Collections.singletonList(AiDramaAudioTypeEnum.SAMPLE_SELECTED.getType())), typeCaptor.capture());
        assertEquals(AiDramaAudioTypeEnum.SAMPLE.getType(), typeCaptor.getValue().getType());
        // 断言：选用当前样音
        ArgumentCaptor<AiDramaAudioDO> captor = ArgumentCaptor.forClass(AiDramaAudioDO.class);
        verify(audioMapper).updateById(captor.capture());
        assertEquals(AiDramaAudioTypeEnum.SAMPLE_SELECTED.getType(), captor.getValue().getType());
    }

    @Test
    public void testSelectAudioMy_narration() {
        when(audioMapper.selectById(9L)).thenReturn(new AiDramaAudioDO().setId(9L).setProjectId(PROJECT_ID)
                .setType(AiDramaAudioTypeEnum.NARRATION.getType()));

        assertServiceException(() -> assetService.selectAudioMy(USER_ID, 9L), DRAMA_AUDIO_TYPE_ERROR, "只有角色样音可以选用");
    }

    // ========== 工具 ==========

    private void mockAppearanceOwner(AiDramaCharacterAppearanceDO appearance) {
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenReturn(appearance);
        when(characterMapper.selectById(CHARACTER_ID)).thenReturn(character());
    }

    private static AiDramaCharacterAppearanceDO appearance() {
        return new AiDramaCharacterAppearanceDO().setId(APPEARANCE_ID).setCharacterId(CHARACTER_ID).setAppearanceIndex(1);
    }

    private static AiDramaCharacterDO character() {
        return new AiDramaCharacterDO().setId(CHARACTER_ID).setProjectId(PROJECT_ID).setName("林夏")
                .setDescription("短发女孩").setReferenceImageUrl("https://files.example.com/ref.png");
    }

    private static AiModelDO imageModel(String platform) {
        return new AiModelDO().setId(IMAGE_MODEL_ID).setName("绘画模型").setType(AiModelTypeEnum.IMAGE.getType())
                .setPlatform(platform).setModel("dall-e-3");
    }

    private static AiDramaImageGenerateReqVO imageReqVO() {
        AiDramaImageGenerateReqVO reqVO = new AiDramaImageGenerateReqVO();
        reqVO.setId(APPEARANCE_ID);
        reqVO.setModelId(IMAGE_MODEL_ID);
        return reqVO;
    }

}
