package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * {@link AiImageServiceImpl} 的单元测试类
 *
 * 重点覆盖 Midjourney 任务与模型（Midjourney Proxy）的绑定：
 * 1. {@link AiImageServiceImpl#midjourneyAction(Long, AiMidjourneyActionReqVO)} 生成的 image，继承原 image 的模型
 * 2. {@link AiImageServiceImpl#midjourneySync()} 按照模型分组，使用各自的 API 查询任务进展
 * 3. {@link AiImageServiceImpl#midjourneyImagine(Long, AiMidjourneyImagineReqVO)} 绘图参数（--ar、--v 等）拼接到 prompt 中提交
 *
 * @author 芋道源码
 */
public class AiImageServiceImplUnitTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long IMAGE_ID = 10L;
    private static final Long MODEL_ID = 101L;
    private static final Long OTHER_MODEL_ID = 202L;
    private static final String CUSTOM_ID = "MJ::JOB::upsample::1::task-a";
    private static final String NEW_TASK_ID = "task-new";

    @InjectMocks
    private AiImageServiceImpl imageService;

    @Mock
    private AiModelService modelService;
    @Mock
    private AiImageMapper imageMapper;
    @Mock
    private FileApi fileApi;

    @Mock
    private MidjourneyApi midjourneyApi;
    @Mock
    private MidjourneyApi otherMidjourneyApi;

    // ========== midjourneyImagine ==========

    @ParameterizedTest(name = "{0}")
    @MethodSource("imagineModels")
    public void testMidjourneyImagine_success(String modelCode, String version, String expectedVersionParam,
                                              String unexpectedParam, String expectedState) {
        // mock 方法（模型）
        AiModelDO model = mockImagineModel(modelCode);
        when(midjourneyApi.imagine(any(MidjourneyApi.ImagineRequest.class))).thenReturn(
                new MidjourneyApi.SubmitResponse(MidjourneyApi.SubmitCodeEnum.SUBMIT_SUCCESS.getCode(), "提交成功", null, NEW_TASK_ID));

        // 调用
        imageService.midjourneyImagine(USER_ID, buildImagineReqVO(version));
        // 断言：绘图参数拼接到 prompt 中提交，state 保持原自定义参数
        verify(midjourneyApi).imagine(argThat(request -> request.getPrompt().startsWith("一只猫 ")
                && request.getPrompt().contains("--ar 1024:768")
                && request.getPrompt().contains(expectedVersionParam)
                && !request.getPrompt().contains(unexpectedParam)
                && Objects.equals(request.getState(), expectedState)
                && request.getBase64Array() == null && request.getNotifyHook() == null));
        // 断言：保存的 image 仍是用户原始 prompt，以及模型绑定
        verify(imageMapper).insert(argThat((AiImageDO image) -> Objects.equals(image.getPrompt(), "一只猫")
                && Objects.equals(image.getUserId(), USER_ID)
                && Objects.equals(image.getModelId(), MODEL_ID)
                && Objects.equals(image.getModel(), model.getName())
                && Objects.equals(image.getPlatform(), AiPlatformEnum.MIDJOURNEY.getPlatform())
                && Objects.equals(image.getWidth(), 1024) && Objects.equals(image.getHeight(), 768)
                && Objects.equals(image.getStatus(), AiImageStatusEnum.IN_PROGRESS.getStatus())
                && Boolean.FALSE.equals(image.getPublicStatus())));
        verify(imageMapper).updateById(argThat((AiImageDO update) -> Objects.equals(update.getTaskId(), NEW_TASK_ID)
                && update.getPrompt() == null));
        verifyNoInteractions(fileApi);
    }

    private static Stream<Arguments> imagineModels() {
        return Stream.of(
                Arguments.of("midjourney", "6.0", "--v 6.0", "--niji", " --ar 1024:768  --v 6.0 "),
                Arguments.of("niji", "6", "--niji 6", "--v ", " --ar 1024:768  --niji 6 "));
    }

    @Test
    public void testMidjourneyImagine_submitFail() {
        // mock 方法：Midjourney Proxy 拒绝
        mockImagineModel("midjourney");
        when(midjourneyApi.imagine(any(MidjourneyApi.ImagineRequest.class))).thenReturn(
                new MidjourneyApi.SubmitResponse("4", "提示词不合法", null, null));

        // 调用，并断言异常
        assertServiceException(() -> imageService.midjourneyImagine(USER_ID, buildImagineReqVO("6.0")),
                IMAGE_MIDJOURNEY_SUBMIT_FAIL, "提示词不合法");
        // 断言：只保存了 image，没有更新 taskId
        verify(imageMapper).insert(any(AiImageDO.class));
        verifyNoMoreInteractions(imageMapper);
        verifyNoInteractions(fileApi);
    }

    private AiModelDO mockImagineModel(String modelCode) {
        AiModelDO model = new AiModelDO().setId(MODEL_ID).setName("Midjourney 绘图").setModel(modelCode)
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform());
        when(modelService.validateModel(MODEL_ID)).thenReturn(model);
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        return model;
    }

    private static AiMidjourneyImagineReqVO buildImagineReqVO(String version) {
        return new AiMidjourneyImagineReqVO().setPrompt("一只猫").setModelId(MODEL_ID)
                .setWidth(1024).setHeight(768).setVersion(version); // referImageUrl 为空，不下载参考图
    }

    // ========== midjourneyAction ==========

    @Test
    public void testMidjourneyAction_success() {
        // mock 数据
        AiImageDO image = buildActionImage(USER_ID);
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(image);
        // mock 方法
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.action(any(MidjourneyApi.ActionRequest.class))).thenReturn(
                new MidjourneyApi.SubmitResponse(MidjourneyApi.SubmitCodeEnum.SUBMIT_SUCCESS.getCode(), "提交成功", null, NEW_TASK_ID));

        // 调用
        imageService.midjourneyAction(USER_ID, buildActionReqVO());
        // 断言：提交原 image 的任务编号，以及所选按钮的 customId
        verify(midjourneyApi).action(argThat(request -> Objects.equals(request.getTaskId(), image.getTaskId())
                && Objects.equals(request.getCustomId(), CUSTOM_ID)
                && request.getNotifyHook() == null));
        // 断言：新 image 继承原 image 的模型，以及其它绑定字段
        verify(imageMapper).insert(argThat((AiImageDO newImage) -> newImage.getId() == null
                && Objects.equals(newImage.getModelId(), MODEL_ID)
                && Objects.equals(newImage.getModel(), image.getModel())
                && Objects.equals(newImage.getUserId(), USER_ID)
                && Objects.equals(newImage.getPrompt(), image.getPrompt())
                && Objects.equals(newImage.getPlatform(), AiPlatformEnum.MIDJOURNEY.getPlatform())
                && Objects.equals(newImage.getWidth(), image.getWidth())
                && Objects.equals(newImage.getHeight(), image.getHeight())
                && Objects.equals(newImage.getOptions(), image.getOptions())
                && Objects.equals(newImage.getTaskId(), NEW_TASK_ID)
                && Objects.equals(newImage.getStatus(), AiImageStatusEnum.IN_PROGRESS.getStatus())
                && Boolean.FALSE.equals(newImage.getPublicStatus())));
    }

    @Test
    public void testMidjourneyAction_notOwner() {
        // mock 数据：其它用户的 image
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildActionImage(USER_ID + 1));

        // 调用，并断言异常
        assertServiceException(() -> imageService.midjourneyAction(USER_ID, buildActionReqVO()), IMAGE_NOT_EXISTS);
        assertActionNotInserted();
        verifyNoInteractions(modelService);
    }

    @Test
    public void testMidjourneyAction_customIdNotExists() {
        // mock 数据
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildActionImage(USER_ID));
        // mock 方法
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);

        // 调用，并断言异常
        assertServiceException(() -> imageService.midjourneyAction(USER_ID, buildActionReqVO().setCustomId("MJ::JOB::other")),
                IMAGE_CUSTOM_ID_NOT_EXISTS);
        assertActionNotInserted();
        verifyNoInteractions(midjourneyApi);
    }

    @Test
    public void testMidjourneyAction_submitFail() {
        // mock 数据
        when(imageMapper.selectById(IMAGE_ID)).thenReturn(buildActionImage(USER_ID));
        // mock 方法：Midjourney Proxy 拒绝
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.action(any(MidjourneyApi.ActionRequest.class))).thenReturn(
                new MidjourneyApi.SubmitResponse("4", "任务不存在", null, null));

        // 调用，并断言异常
        assertServiceException(() -> imageService.midjourneyAction(USER_ID, buildActionReqVO()),
                IMAGE_MIDJOURNEY_SUBMIT_FAIL, "任务不存在");
        assertActionNotInserted();
    }

    private AiImageDO buildActionImage(Long userId) {
        return new AiImageDO().setId(IMAGE_ID).setUserId(userId).setPrompt("一只猫")
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform()).setModelId(MODEL_ID).setModel("midjourney")
                .setWidth(1024).setHeight(768).setOptions(Map.of("version", "6.0"))
                .setStatus(AiImageStatusEnum.SUCCESS.getStatus()).setPublicStatus(true).setTaskId("task-a")
                .setButtons(Collections.singletonList(new MidjourneyApi.Button(CUSTOM_ID, null, "U1", null, null)));
    }

    private static AiMidjourneyActionReqVO buildActionReqVO() {
        return new AiMidjourneyActionReqVO().setId(IMAGE_ID).setCustomId(CUSTOM_ID);
    }

    /**
     * 断言：只查询了 image，没有新增 image
     */
    private void assertActionNotInserted() {
        verify(imageMapper).selectById(IMAGE_ID);
        verifyNoMoreInteractions(imageMapper);
    }

    // ========== midjourneySync ==========

    @Test
    public void testMidjourneySync_empty() {
        // mock 数据
        mockInProgressImages();

        // 调用，并断言
        assertEquals(0, imageService.midjourneySync());
        verifyNoInteractions(modelService);
    }

    @Test
    public void testMidjourneySync_singleModel() {
        // mock 数据
        mockInProgressImages(buildSyncImage(1L, MODEL_ID, "task-a"), buildSyncImage(2L, MODEL_ID, "task-b"));
        // mock 方法
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-a", "task-b"))).thenReturn(Arrays.asList(
                buildNotify("task-a", MidjourneyApi.TaskStatusEnum.SUCCESS, null),
                buildNotify("task-b", MidjourneyApi.TaskStatusEnum.FAILURE, "被拒绝")));

        // 调用，并断言
        assertEquals(2, imageService.midjourneySync());
        verifyImageUpdated(1L, AiImageStatusEnum.SUCCESS, null);
        verifyImageUpdated(2L, AiImageStatusEnum.FAIL, "被拒绝");
        assertSyncNoMoreWrite();
    }

    @Test
    public void testMidjourneySync_multiModel() {
        // mock 数据：两个模型，各自对应不同的 Midjourney Proxy
        mockInProgressImages(buildSyncImage(1L, MODEL_ID, "task-a"), buildSyncImage(2L, OTHER_MODEL_ID, "task-b"));
        // mock 方法：每个 API 只查询自己模型的任务
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-a"))).thenReturn(Collections.singletonList(
                buildNotify("task-a", MidjourneyApi.TaskStatusEnum.SUCCESS, null)));
        when(modelService.getMidjourneyApi(OTHER_MODEL_ID)).thenReturn(otherMidjourneyApi);
        when(otherMidjourneyApi.getTaskList(Set.of("task-b"))).thenReturn(Collections.singletonList(
                buildNotify("task-b", MidjourneyApi.TaskStatusEnum.SUCCESS, null)));

        // 调用，并断言
        assertEquals(2, imageService.midjourneySync());
        verifyImageUpdated(1L, AiImageStatusEnum.SUCCESS, null);
        verifyImageUpdated(2L, AiImageStatusEnum.SUCCESS, null);
        assertSyncNoMoreWrite();
    }

    @Test
    public void testMidjourneySync_sameTaskIdInDifferentModels() {
        // mock 数据：不同 Midjourney Proxy 中，相同的 taskId
        mockInProgressImages(buildSyncImage(1L, MODEL_ID, "task-same"), buildSyncImage(2L, OTHER_MODEL_ID, "task-same"));
        // mock 方法：两个 Proxy 返回不同的结果
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-same"))).thenReturn(Collections.singletonList(
                buildNotify("task-same", MidjourneyApi.TaskStatusEnum.SUCCESS, null)));
        when(modelService.getMidjourneyApi(OTHER_MODEL_ID)).thenReturn(otherMidjourneyApi);
        when(otherMidjourneyApi.getTaskList(Set.of("task-same"))).thenReturn(Collections.singletonList(
                buildNotify("task-same", MidjourneyApi.TaskStatusEnum.FAILURE, "被拒绝")));

        // 调用，并断言：各自使用自己 Proxy 的结果，不串
        assertEquals(2, imageService.midjourneySync());
        verifyImageUpdated(1L, AiImageStatusEnum.SUCCESS, null);
        verifyImageUpdated(2L, AiImageStatusEnum.FAIL, "被拒绝");
        assertSyncNoMoreWrite();
    }

    @Test
    public void testMidjourneySync_unboundImageFirst() {
        // mock 数据：未关联模型的 image 排在第一个
        mockInProgressImages(buildSyncImage(1L, null, "task-x"), buildSyncImage(2L, MODEL_ID, "task-a"));
        // mock 方法
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-a"))).thenReturn(Collections.singletonList(
                buildNotify("task-a", MidjourneyApi.TaskStatusEnum.SUCCESS, null)));

        // 调用，并断言：跳过未关联模型的 image，其它 image 继续同步
        assertEquals(1, imageService.midjourneySync());
        verifyImageUpdated(2L, AiImageStatusEnum.SUCCESS, null);
        assertSyncNoMoreWrite();
        verifyNoMoreInteractions(modelService);
    }

    @Test
    public void testMidjourneySync_allUnbound() {
        // mock 数据：全部未关联模型
        mockInProgressImages(buildSyncImage(1L, null, "task-x"), buildSyncImage(2L, null, "task-y"));

        // 调用，并断言：不查询 API，不更新 image
        assertEquals(0, imageService.midjourneySync());
        verifyNoInteractions(modelService);
        assertSyncNoMoreWrite();
    }

    @Test
    public void testMidjourneySync_taskNotFound() {
        // mock 数据
        mockInProgressImages(buildSyncImage(1L, MODEL_ID, "task-a"), buildSyncImage(2L, MODEL_ID, "task-b"));
        // mock 方法：Proxy 只返回 task-a 的进展
        when(modelService.getMidjourneyApi(MODEL_ID)).thenReturn(midjourneyApi);
        when(midjourneyApi.getTaskList(Set.of("task-a", "task-b"))).thenReturn(Collections.singletonList(
                buildNotify("task-a", MidjourneyApi.TaskStatusEnum.SUCCESS, null)));

        // 调用，并断言：只跳过查询不到的 image
        assertEquals(1, imageService.midjourneySync());
        verifyImageUpdated(1L, AiImageStatusEnum.SUCCESS, null);
        assertSyncNoMoreWrite();
    }

    private void mockInProgressImages(AiImageDO... images) {
        when(imageMapper.selectListByStatusAndPlatform(AiImageStatusEnum.IN_PROGRESS.getStatus(),
                AiPlatformEnum.MIDJOURNEY.getPlatform())).thenReturn(Arrays.asList(images));
    }

    private static AiImageDO buildSyncImage(Long id, Long modelId, String taskId) {
        return new AiImageDO().setId(id).setModelId(modelId).setTaskId(taskId)
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform()).setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus());
    }

    /**
     * 构建任务进展。imageUrl 为空，不触发图片下载
     */
    private static MidjourneyApi.Notify buildNotify(String taskId, MidjourneyApi.TaskStatusEnum status, String failReason) {
        return new MidjourneyApi.Notify(taskId, null, status.name(), null, null, null, null,
                null, null, null, null, null, failReason, null);
    }

    private void verifyImageUpdated(Long id, AiImageStatusEnum status, String errorMessage) {
        verify(imageMapper).updateById(argThat((AiImageDO update) -> Objects.equals(update.getId(), id)
                && Objects.equals(update.getStatus(), status.getStatus())
                && Objects.equals(update.getErrorMessage(), errorMessage)
                && update.getFinishTime() != null));
    }

    /**
     * 断言：除已校验的更新外，没有其它 image 写入，也没有图片下载上传
     */
    private void assertSyncNoMoreWrite() {
        verify(imageMapper).selectListByStatusAndPlatform(AiImageStatusEnum.IN_PROGRESS.getStatus(),
                AiPlatformEnum.MIDJOURNEY.getPlatform());
        verifyNoMoreInteractions(imageMapper);
        verifyNoInteractions(fileApi);
    }

}
