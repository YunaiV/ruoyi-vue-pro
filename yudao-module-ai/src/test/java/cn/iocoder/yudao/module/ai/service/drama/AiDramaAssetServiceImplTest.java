package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.AiDramaAppearanceSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioMusicReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.AiDramaImageGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.*;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStyleEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaVisualAssetKindEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.music.AiMusicService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillFileService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillService;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils;
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
    private AiDramaMotionReferenceMapper motionReferenceMapper;
    @Mock
    private AiDramaProjectService projectService;
    @Mock
    private AiModelService modelService;
    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private AiMusicService musicService;
    @Mock
    private AiDramaScriptService scriptService;
    @Mock
    private AiSkillService skillService;
    @Mock
    private AiSkillFileService skillFileService;
    @Mock
    private FileApi fileApi;

    // ========== 候选图、历史版本 ==========

    @Test
    public void testArchiveVisualAsset_shotFrame() {
        AiDramaVisualAssetDO current = new AiDramaVisualAssetDO().setId(5L).setProjectId(PROJECT_ID).setStoryboardId(7L)
                .setKind(AiDramaVisualAssetKindEnum.SHOT_FRAME.getType()).setPrompt("提示词")
                .setImageUrl("https://files.example.com/k.png").setStatus(AiDramaStatusEnum.SUCCESS.getStatus());

        assetService.archiveVisualAsset(current);

        // 断言：复制为历史版本，指向当前版本
        ArgumentCaptor<AiDramaVisualAssetDO> captor = ArgumentCaptor.forClass(AiDramaVisualAssetDO.class);
        verify(visualAssetMapper).insert(captor.capture());
        assertEquals(5L, captor.getValue().getParentId());
        assertEquals(AiDramaVisualAssetKindEnum.ARCHIVED_SHOT_FRAME.getType(), captor.getValue().getKind());
        assertEquals(7L, captor.getValue().getStoryboardId());
        assertEquals("https://files.example.com/k.png", captor.getValue().getImageUrl());
        assertNull(captor.getValue().getId());
    }

    @Test
    public void testArchiveVisualAsset_noImage() {
        assetService.archiveVisualAsset(new AiDramaVisualAssetDO().setId(5L).setKind(AiDramaVisualAssetKindEnum.PROP.getType()));

        verify(visualAssetMapper, never()).insert(any(AiDramaVisualAssetDO.class));
    }

    @Test
    public void testBuildImagePrompt() {
        assertEquals("林夏，短发，画风：电影感，人物设定图",
                AiDramaAssetServiceImpl.buildImagePrompt("电影感", "人物设定图", "林夏", " 短发 ", null, " "));
    }

    @Test
    public void testBuildStyleText_scriptToneAndSkillMedia() {
        when(scriptService.getScriptByProjectId(PROJECT_ID)).thenReturn(new AiDramaScriptDO().setId(1L).setProjectId(PROJECT_ID)
                .setTone("冷峻悬疑").setWorldbuilding("近未来城市"));
        when(skillService.getSkill(5L)).thenReturn(AiSkillDO.builder().id(5L).name("胶片").description("技能描述").build());
        when(skillFileService.getSkillFileListBySkillId(5L)).thenReturn(Collections.singletonList(AiSkillFileDO.builder()
                .parentId(AiSkillFileDO.PARENT_ID_ROOT).name(AiSkillFileDO.NAME_SKILL)
                .content("---\nname: film\n---\n# 胶片\n长篇说明\n## 媒体提示词\n35mm 胶片颗粒，低饱和\n## 其它\n不进入媒体").build()));
        AiDramaProjectDO project = new AiDramaProjectDO().setId(PROJECT_ID)
                .setStyle(AiDramaStyleEnum.SCRIPT_TONE.getStyle()).setAestheticSkillId(5L);

        // 断言：随剧本基调取基调；带上世界观；技能取媒体提示词段，而不是描述
        assertEquals("冷峻悬疑，世界观：近未来城市，35mm 胶片颗粒，低饱和", assetService.buildStyleText(project));
    }

    @Test
    public void testUpdateAppearanceMy_selectedIndexInvalid() {
        mockAppearanceOwner(appearance().setImageUrls(Collections.emptyList()));
        AiDramaAppearanceSaveReqVO reqVO = new AiDramaAppearanceSaveReqVO();
        reqVO.setId(APPEARANCE_ID);
        reqVO.setSelectedIndex(0);

        // 超出候选图的下标不能选用
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
        when(projectService.validateProjectExists(PROJECT_ID)).thenReturn(new AiDramaProjectDO().setId(PROJECT_ID)
                .setStyle(AiDramaStyleEnum.REALISTIC.getStyle()));
        doNothing().when(assetService).executeImage(any(), any(), any(), any(), any(), any());

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            springUtil.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(assetService);
            assetService.generateAppearanceImageMy(USER_ID, imageReqVO());
        }

        // 断言：记录本次的模型，置为生成中
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).updateById(captor.capture());
        assertEquals(AiDramaStatusEnum.IN_PROGRESS.getStatus(), captor.getValue().getStatus());
        assertEquals(IMAGE_MODEL_ID, captor.getValue().getModelId());
        // 断言：异步出图，带上记录的任务编号；参考图取角色的参考图
        ArgumentCaptor<String> taskIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(assetService).executeImage(eq(AiDramaTaskTypeEnum.APPEARANCE), eq(APPEARANCE_ID), taskIdCaptor.capture(),
                any(), argThat((String prompt) -> prompt.contains("林夏") && prompt.contains("画风：真人写实")), eq(Collections.singletonList("https://files.example.com/ref.png")));
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
        when(appearanceMapper.selectById(APPEARANCE_ID)).thenReturn(appearance());
        AiDramaAssetServiceImpl.ImageClient client = new AiDramaAssetServiceImpl.ImageClient(
                imageModel(AiPlatformEnum.OPENAI.getPlatform()), null, imageModel);

        try (MockedStatic<HttpUtil> httpUtil = mockStatic(HttpUtil.class)) {
            httpUtil.when(() -> HttpUtil.downloadBytes("https://provider.example.com/tmp.png")).thenReturn(new byte[]{1});
            assetService.executeImage(AiDramaTaskTypeEnum.APPEARANCE, APPEARANCE_ID, "task-1", client, "提示词",
                    Collections.emptyList());
        }

        // 断言：追加候选图与说明，未选用时默认选用；以生成中、本次任务编号为条件
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).updateByIdAndStatusAndTaskId(eq(APPEARANCE_ID), eq(AiDramaStatusEnum.IN_PROGRESS.getStatus()),
                eq("task-1"), captor.capture());
        assertEquals(AiDramaStatusEnum.SUCCESS.getStatus(), captor.getValue().getStatus());
        assertEquals(Arrays.asList("https://files.example.com/new.png"), captor.getValue().getImageUrls());
        assertEquals(Arrays.asList("提示词"), captor.getValue().getImageDescriptions());
        assertEquals(Collections.emptyList(), captor.getValue().getPreviousImageUrls());
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
                .setStatus(dbStatus.get()).setTaskId(dbTaskId.get()));
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
        assertEquals(Arrays.asList("https://files.example.com/new.png"), written.get(0).getImageUrls());
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

        // 断言：同时创建第 1 个形象，序号从 0 开始
        ArgumentCaptor<AiDramaCharacterAppearanceDO> captor = ArgumentCaptor.forClass(AiDramaCharacterAppearanceDO.class);
        verify(appearanceMapper).insert(captor.capture());
        assertEquals(CHARACTER_ID, captor.getValue().getCharacterId());
        assertEquals(0, captor.getValue().getAppearanceIndex());
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
        when(appearanceMapper.selectListByStatusAndUpdateTimeLt(eq(inProgress), any())).thenReturn(Arrays.asList(
                appearance().setStatus(inProgress).setTaskId("task-1")));
        when(appearanceMapper.updateByIdAndStatusAndTaskId(eq(APPEARANCE_ID), eq(inProgress), eq("task-1"), any())).thenReturn(1);
        when(visualAssetMapper.selectListByStatusAndUpdateTimeLt(eq(inProgress), any())).thenReturn(Arrays.asList(
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

        verify(appearanceMapper).deleteByCharacterIds(Arrays.asList(CHARACTER_ID));
        verify(characterMapper).deleteByProjectId(PROJECT_ID);
        verify(locationMapper).deleteByProjectId(PROJECT_ID);
        verify(visualAssetMapper).deleteByProjectId(PROJECT_ID);
        verify(audioMapper).deleteByProjectId(PROJECT_ID);
        verify(motionReferenceMapper).deleteByProjectId(PROJECT_ID);
    }

    // ========== 音频 ==========

    @Test
    public void testCreateAudioMy_dialogueNotAllowed() {
        AiDramaAudioCreateReqVO reqVO = new AiDramaAudioCreateReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setCharacterId(CHARACTER_ID);
        reqVO.setType(AiDramaAudioTypeEnum.DIALOGUE.getType());
        reqVO.setFileUrl("https://files.example.com/a.mp3");

        assertServiceException(() -> assetService.createAudioMy(USER_ID, reqVO),
                DRAMA_AUDIO_TYPE_ERROR, AiDramaAudioTypeEnum.DIALOGUE.getType());
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

        // 断言：记录为角色选用的样音，音频类型不变
        ArgumentCaptor<AiDramaCharacterDO> captor = ArgumentCaptor.forClass(AiDramaCharacterDO.class);
        verify(characterMapper).updateById(captor.capture());
        assertEquals(CHARACTER_ID, captor.getValue().getId());
        assertEquals(9L, captor.getValue().getSampleAudioId());
        verify(audioMapper, never()).updateById(any(AiDramaAudioDO.class));
    }

    @Test
    public void testSelectAudioMy_narration() {
        when(audioMapper.selectById(9L)).thenReturn(new AiDramaAudioDO().setId(9L).setProjectId(PROJECT_ID)
                .setType(AiDramaAudioTypeEnum.NARRATION.getType()));

        assertServiceException(() -> assetService.selectAudioMy(USER_ID, 9L), DRAMA_AUDIO_TYPE_ERROR, "只有角色样音可以选用");
    }

    @Test
    public void testCreateMusicAudioMy_success() {
        when(musicService.getMusic(3L)).thenReturn(new AiMusicDO().setId(3L).setUserId(USER_ID).setTitle("雨夜")
                .setStatus(AiMusicStatusEnum.SUCCESS.getStatus()).setAudioUrl("https://files.example.com/m.mp3").setDuration(30D));
        AiDramaAudioMusicReqVO reqVO = new AiDramaAudioMusicReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setMusicId(3L);
        reqVO.setStoryboardIds(Arrays.asList(7L));

        assetService.createMusicAudioMy(USER_ID, reqVO);

        // 断言：作为配乐插入，复制音乐的地址、时长，并记录来源音乐
        ArgumentCaptor<AiDramaAudioDO> captor = ArgumentCaptor.forClass(AiDramaAudioDO.class);
        verify(audioMapper).insert(captor.capture());
        assertEquals(AiDramaAudioTypeEnum.BGM.getType(), captor.getValue().getType());
        assertEquals("雨夜", captor.getValue().getName());
        assertEquals("https://files.example.com/m.mp3", captor.getValue().getFileUrl());
        assertEquals(30D, captor.getValue().getDuration());
        assertEquals(Arrays.asList(7L), captor.getValue().getStoryboardIds());
        assertEquals(3L, captor.getValue().getMusicId());
    }

    @Test
    public void testCreateMusicAudioMy_notReady() {
        when(musicService.getMusic(3L)).thenReturn(new AiMusicDO().setId(3L).setUserId(USER_ID)
                .setStatus(AiMusicStatusEnum.IN_PROGRESS.getStatus()));
        AiDramaAudioMusicReqVO reqVO = new AiDramaAudioMusicReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setMusicId(3L);

        assertServiceException(() -> assetService.createMusicAudioMy(USER_ID, reqVO), DRAMA_MUSIC_NOT_READY);
        verify(audioMapper, never()).insert(any(AiDramaAudioDO.class));
    }

    // ========== 动作参考 ==========

    @Test
    public void testValidateMotionVideo() {
        // 合规：5 秒、30fps、720x1280 H.264
        AiDramaAssetServiceImpl.validateMotionVideo(new AiFfmpegUtils.VideoInfo(5, 720, 1280, 30, "h264"));
        // 不合规：时长过长、帧率过低、分辨率过低、编码不支持
        assertThrows(Exception.class, () -> AiDramaAssetServiceImpl.validateMotionVideo(
                new AiFfmpegUtils.VideoInfo(31, 720, 1280, 30, "h264")));
        assertThrows(Exception.class, () -> AiDramaAssetServiceImpl.validateMotionVideo(
                new AiFfmpegUtils.VideoInfo(5, 720, 1280, 15, "h264")));
        assertThrows(Exception.class, () -> AiDramaAssetServiceImpl.validateMotionVideo(
                new AiFfmpegUtils.VideoInfo(5, 360, 640, 30, "h264")));
        assertThrows(Exception.class, () -> AiDramaAssetServiceImpl.validateMotionVideo(
                new AiFfmpegUtils.VideoInfo(5, 720, 1280, 30, "vp9")));
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
