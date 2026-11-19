package cn.iocoder.yudao.module.ai.service.video;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.video.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.dal.mysql.video.AiVideoMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoModeEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoStatusEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoOutput;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao.DouBaoSeedanceVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao.DouBaoSeedanceVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng.JimengDreamActorRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng.JimengOmniHumanRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng.JimengVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng.JimengVisualApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.kling.KlingVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.kling.KlingVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxH3VideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxH3VideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxHailuoVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxHailuoVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.HappyHorseVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.HappyHorseVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.TongYiWanVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.TongYiWanVideoRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.veo.VertexVeoVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.veo.VertexVeoVideoRequest;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiVideoUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 视频 Service 实现类
 *
 * 视频的生成流程：
 * 1. 提交：先在本地校验请求，再插入待处理（PENDING）的记录，提交服务商取得任务编号后，更新为生成中（IN_PROGRESS）。
 *    提交结果未知（超时、网络中断）时直接置为已失败，提示不要重复提交，不自动重新提交
 * 2. 查询：定时查询生成中、转存中的任务，服务商成功的更新为转存中（SAVING），参见 {@link #fetchVideo()}
 * 3. 转存：下载服务商的临时地址并保存到文件服务后，更新为已完成（SUCCESS）；
 *    转存失败时保持转存中，下轮按任务编号重新查询获得新地址再转存，不会重新生成
 * 4. 失败：服务商确认失败，或超过 {@link #TIMEOUT_MINUTES} 分钟未完成，更新为已失败（FAIL），参见 {@link #timeoutVideo()}
 *
 * 服务商按平台、模型选择已有的视频客户端：
 * 1. 豆包（BYTEDANCE）：模型以 jimeng_ 开头、或数字人口播 / 动作模仿，走即梦 Visual（密钥 AccessKeyId|SecretAccessKey 或 IAM API Key）；其余走方舟 Seedance
 * 2. 可灵（KLING）：kling-v 开头的走旧版接口，其余走新版接口
 * 3. MiniMax：MiniMax-H3 开头的走 v2 接口，其余走海螺 v1 接口
 * 4. 阿里（ALIBABA）：happyhorse 开头的走 HappyHorse，其余走通义万相
 * 5. Google（GEMINI）：veo- 开头的走 Vertex AI Veo；密钥的地址填写 Vertex AI 地址（含项目、地域），API 密钥填写 OAuth 访问令牌
 * 查询所需的路由（可灵、即梦）由模型与输入推导，不另存字段；路由判断、客户端创建见 {@link AiVideoUtils}
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class AiVideoServiceImpl implements AiVideoService {

    /**
     * 任务超时时间：从创建时间起算
     */
    @VisibleForTesting
    static final int TIMEOUT_MINUTES = 60;
    /**
     * 每轮最多查询的任务数
     */
    private static final int SYNC_LIMIT = 100;

    @Resource
    private AiVideoMapper videoMapper;

    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private FileApi fileApi;

    // ========== 创作 ==========

    @Override
    public Long generateVideo(Long userId, AiVideoGenerateReqVO reqVO) {
        // 1. 校验模型与其当前密钥，以及创作类型
        AiModelDO model = validateVideoModel(reqVO.getModelId());
        AiApiKeyDO apiKey = validateVideoApiKey(model);
        validateVideoType(reqVO.getType(), model);

        // 2.1 构建记录：生成方式只用于视频生成，默认文生；输入文件只保留类型、方式对应的角色
        boolean generate = Objects.equals(reqVO.getType(), AiVideoTypeEnum.GENERATE.getType());
        String mode = generate ? StrUtil.blankToDefault(reqVO.getMode(), AiVideoModeEnum.TEXT.getMode()) : null;
        AiVideoDO video = BeanUtils.toBean(reqVO, AiVideoDO.class, o -> o.setUserId(userId).setMode(mode)
                .setPlatform(model.getPlatform()).setModel(model.getModel()).setApiKeyId(apiKey.getId())
                .setPrompt(StrUtil.trimToNull(reqVO.getPrompt()))
                .setInputUrls(AiVideoUtils.filterInputUrls(reqVO.getType(), mode, reqVO.getInputUrls()))
                .setStatus(AiVideoStatusEnum.PENDING.getStatus()).setProgress(0).setPublicStatus(false));
        if (!generate) {
            // 数字人口播、动作模仿不使用分辨率、比例、时长：不保存，完成后取服务商返回的时长
            video.setResolution(null).setRatio(null).setDuration(null);
        }
        // 2.2 本地构建并校验请求：不通过直接报错，不插入记录
        Supplier<AiVideoTaskRef> submitter = buildVideoSubmitter(video, apiKey);

        // 3.1 插入记录
        videoMapper.insert(video);
        // 3.2 提交服务商
        submitVideo(video, submitter);
        return video.getId();
    }

    @Override
    public AiModelDO validateVideoModel(Long modelId) {
        AiModelDO model = modelService.validateModel(modelId);
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.VIDEO.getType())) {
            throw exception(VIDEO_MODEL_TYPE_ERROR);
        }
        return model;
    }

    @Override
    public AiApiKeyDO validateVideoApiKey(AiModelDO model) {
        AiApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        if (ObjUtil.notEqual(model.getPlatform(), apiKey.getPlatform())) {
            throw exception(VIDEO_MODEL_PLATFORM_MISMATCH, model.getPlatform(), apiKey.getPlatform());
        }
        return apiKey;
    }

    /**
     * 校验创作类型与模型：数字人口播、动作模仿只支持字节跳动（即梦）的模型；OmniHuman 1.5 需要先完成主体识别，暂不支持
     */
    private static void validateVideoType(Integer type, AiModelDO model) {
        if (Objects.equals(type, AiVideoTypeEnum.GENERATE.getType())) {
            return;
        }
        if (ObjUtil.notEqual(model.getPlatform(), AiPlatformEnum.BYTEDANCE.getPlatform())) {
            throw exception(VIDEO_PARAMS_INVALID, "数字人口播、动作模仿目前只支持字节跳动（即梦）的模型");
        }
        if (Objects.equals(type, AiVideoTypeEnum.AVATAR.getType())
                && JimengVisualApi.REQ_KEY_OMNI_HUMAN_V15.equals(model.getModel())) {
            throw exception(VIDEO_PARAMS_INVALID, "OmniHuman 1.5 需要先完成主体识别，暂不支持，请使用 OmniHuman 2.0（"
                    + JimengVisualApi.REQ_KEY_OMNI_HUMAN_V20 + "）");
        }
    }

    /**
     * 提交服务商：成功更新为生成中，失败更新为已失败并记录原因；都不抛出，前端按状态展示
     */
    private void submitVideo(AiVideoDO video, Supplier<AiVideoTaskRef> submitter) {
        try {
            // 1. 成功：写入任务编号，更新为生成中
            AiVideoTaskRef ref = submitter.get();
            videoMapper.updateByIdAndStatus(video.getId(), AiVideoStatusEnum.PENDING.getStatus(), new AiVideoDO()
                    .setTaskId(ref.getTaskId()).setStatus(AiVideoStatusEnum.IN_PROGRESS.getStatus()));
        } catch (Exception e) {
            // 2. 失败：更新为已失败；提交结果未知时，提示不要重复提交
            log.error("[submitVideo][video({}) 提交服务商失败]", video.getId(), e);
            updateVideoFail(video.getId(), AiVideoStatusEnum.PENDING.getStatus(), AiVideoException.buildSubmitErrorMessage(e));
        }
    }

    // ========== 我的作品、公开作品 ==========

    @Override
    public PageResult<AiVideoDO> getVideoMyPage(Long userId, AiVideoMyPageReqVO pageReqVO) {
        return videoMapper.selectPageByUserId(userId, pageReqVO.getType(), pageReqVO);
    }

    @Override
    public PageResult<AiVideoDO> getVideoPublicPage(AiVideoPublicPageReqVO pageReqVO) {
        return videoMapper.selectPageByPublic(pageReqVO);
    }

    @Override
    public AiVideoDO getVideoDetail(Long id, Long userId) {
        return validateVideoVisible(id, userId);
    }

    @Override
    public void deleteVideoMy(Long id, Long userId) {
        // 1. 校验：本人、已结束
        validateVideoFinished(validateMyVideoExists(id, userId));

        // 2. 删除
        videoMapper.deleteById(id);
    }

    /**
     * 校验可见：本人，或已公开且已完成；否则统一按不存在处理
     */
    private AiVideoDO validateVideoVisible(Long id, Long userId) {
        AiVideoDO video = validateVideoExists(id);
        if (Objects.equals(video.getUserId(), userId)) {
            return video;
        }
        if (Boolean.TRUE.equals(video.getPublicStatus())
                && Objects.equals(video.getStatus(), AiVideoStatusEnum.SUCCESS.getStatus())) {
            return video;
        }
        throw exception(VIDEO_NOT_EXISTS);
    }

    // ========== 视频生成管理 ==========

    @Override
    public PageResult<AiVideoDO> getVideoPage(AiVideoPageReqVO pageReqVO) {
        return videoMapper.selectPage(pageReqVO);
    }

    @Override
    public AiVideoDO getVideo(Long id) {
        return videoMapper.selectById(id);
    }

    @Override
    public void updateVideo(AiVideoUpdateReqVO updateReqVO) {
        // 校验存在、已完成
        validateVideoSuccess(validateVideoExists(updateReqVO.getId()));
        // 更新
        videoMapper.updateById(new AiVideoDO().setId(updateReqVO.getId()).setPublicStatus(updateReqVO.getPublicStatus()));
    }

    @Override
    public void deleteVideo(Long id) {
        // 校验存在、已结束：未结束的删除后，同步任务会把结果写回已删除的行
        validateVideoFinished(validateVideoExists(id));
        // 删除
        videoMapper.deleteById(id);
    }

    private AiVideoDO validateVideoExists(Long id) {
        AiVideoDO video = videoMapper.selectById(id);
        if (video == null) {
            throw exception(VIDEO_NOT_EXISTS);
        }
        return video;
    }

    /**
     * 校验本人的视频存在：不是本人的，按不存在处理
     */
    private AiVideoDO validateMyVideoExists(Long id, Long userId) {
        AiVideoDO video = validateVideoExists(id);
        if (ObjUtil.notEqual(video.getUserId(), userId)) {
            throw exception(VIDEO_NOT_EXISTS);
        }
        return video;
    }

    private static void validateVideoFinished(AiVideoDO video) {
        if (AiVideoStatusEnum.isRunning(video.getStatus())) {
            throw exception(VIDEO_STATUS_NOT_FINISHED);
        }
    }

    private static void validateVideoSuccess(AiVideoDO video) {
        if (ObjUtil.notEqual(video.getStatus(), AiVideoStatusEnum.SUCCESS.getStatus())) {
            throw exception(VIDEO_STATUS_NOT_SUCCESS);
        }
    }

    // ========== 服务商：提交与查询 ==========

    @Override
    public Supplier<AiVideoTaskRef> buildVideoSubmitter(AiVideoDO video, AiApiKeyDO apiKey) {
        try {
            return doBuildSubmitter(video, apiKey);
        } catch (IllegalArgumentException e) {
            throw exception(VIDEO_PARAMS_INVALID, e.getMessage());
        }
    }

    /**
     * 构建提交服务商的方法：按类型、平台、模型选择客户端，本地校验并构建请求，返回的方法执行时才调用服务商
     *
     * @param video 视频记录
     * @param apiKey API 密钥
     * @return 提交服务商的方法
     * @throws IllegalArgumentException 请求参数不正确
     */
    private Supplier<AiVideoTaskRef> doBuildSubmitter(AiVideoDO video, AiApiKeyDO apiKey) {
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(video.getPlatform());
        AiVideoDO.Options options = ObjUtil.defaultIfNull(video.getOptions(), new AiVideoDO.Options());
        String model = video.getModel();
        // 1. 数字人口播、动作模仿：只支持即梦，平台已在 validateVideoType 校验
        if (ObjUtil.notEqual(video.getType(), AiVideoTypeEnum.GENERATE.getType())) {
            JimengVisualApi api = AiVideoUtils.getJimengApi(apiKey);
            // 1.1 数字人口播：OmniHuman
            if (Objects.equals(video.getType(), AiVideoTypeEnum.AVATAR.getType())) {
                JimengOmniHumanRequest request = new JimengOmniHumanRequest().setReqKey(model)
                        .setImageUrl(getInput(video, AiVideoInputRoleEnum.IMAGE))
                        .setAudioUrl(getInput(video, AiVideoInputRoleEnum.AUDIO))
                        .setPrompt(video.getPrompt()).setSeed(options.getSeed())
                        .setOutputResolution(options.getOutputResolution()).setEnableWatermark(options.getWatermark());
                JimengVisualApi.buildOmniHumanRequestBody(request);
                return () -> api.submitOmniHuman(request);
            }
            // 1.2 动作模仿：DreamActor
            JimengDreamActorRequest request = new JimengDreamActorRequest().setReqKey(model)
                    .setImageUrl(getInput(video, AiVideoInputRoleEnum.IMAGE))
                    .setVideoUrl(getInput(video, AiVideoInputRoleEnum.VIDEO));
            JimengVisualApi.buildDreamActorRequestBody(request);
            return () -> api.submitDreamActor(request);
        }

        // 2. 视频生成：按平台、模型选择客户端
        String firstFrame = getInput(video, AiVideoInputRoleEnum.FIRST_FRAME);
        String lastFrame = getInput(video, AiVideoInputRoleEnum.LAST_FRAME);
        List<String> referenceImages = getInputs(video, AiVideoInputRoleEnum.REFERENCE_IMAGE);
        switch (platform) {
            case BYTEDANCE: {
                // 2.1 字节跳动：即梦视频 3.0，或方舟 Seedance
                if (AiVideoUtils.isJimeng(video)) {
                    validateInputsUnused(video, AiVideoInputRoleEnum.REFERENCE_IMAGE, AiVideoInputRoleEnum.REFERENCE_VIDEO,
                            AiVideoInputRoleEnum.REFERENCE_AUDIO);
                    JimengVideoRequest request = new JimengVideoRequest().setReqKey(model).setPrompt(video.getPrompt())
                            .setSeed(options.getSeed()).setAspectRatio(video.getRatio())
                            .setFrames(AiVideoUtils.getJimengFrames(video.getDuration()));
                    CollUtil.addAll(request.getImageUrls(), CollUtil.removeNull(CollUtil.newArrayList(firstFrame, lastFrame)));
                    JimengVisualApi.buildVideoRequestBody(request);
                    JimengVisualApi api = AiVideoUtils.getJimengApi(apiKey);
                    return () -> api.submitVideo(request);
                }
                DouBaoSeedanceVideoRequest request = new DouBaoSeedanceVideoRequest().setModel(model)
                        .setPrompt(video.getPrompt()).setFirstFrame(firstFrame).setLastFrame(lastFrame)
                        .setResolution(video.getResolution()).setRatio(video.getRatio())
                        .setDuration(video.getDuration()).setSeed(options.getSeed())
                        .setCameraFixed(options.getCameraFixed()).setWatermark(options.getWatermark())
                        .setGenerateAudio(options.getGenerateAudio());
                request.getReferenceImages().addAll(referenceImages);
                request.getReferenceVideos().addAll(getInputs(video, AiVideoInputRoleEnum.REFERENCE_VIDEO));
                request.getReferenceAudios().addAll(getInputs(video, AiVideoInputRoleEnum.REFERENCE_AUDIO));
                DouBaoSeedanceVideoApi.buildRequestBody(request);
                DouBaoSeedanceVideoApi api = AiVideoUtils.getSeedanceApi(apiKey);
                return () -> api.createTask(request);
            }
            case KLING: {
                // 2.2 可灵：旧版、新版接口的请求相同，校验与路径不同
                validateInputsUnused(video, AiVideoInputRoleEnum.REFERENCE_IMAGE, AiVideoInputRoleEnum.REFERENCE_VIDEO,
                        AiVideoInputRoleEnum.REFERENCE_AUDIO);
                KlingVideoRequest request = new KlingVideoRequest().setModel(model).setPrompt(video.getPrompt())
                        .setFirstFrame(firstFrame).setLastFrame(lastFrame)
                        .setResolution(video.getResolution()).setAspectRatio(video.getRatio())
                        .setDuration(video.getDuration()).setAudio(options.getGenerateAudio())
                        .setWatermark(options.getWatermark()).setNegativePrompt(options.getNegativePrompt())
                        .setMode(options.getQuality());
                boolean legacy = AiVideoUtils.isKlingLegacy(model);
                if (legacy) {
                    KlingVideoApi.buildLegacyRequestBody(request);
                } else {
                    KlingVideoApi.buildRequestBody(request);
                }
                KlingVideoApi api = AiVideoUtils.getKlingApi(apiKey);
                return legacy ? () -> api.createLegacyTask(request) : () -> api.createTask(request);
            }
            case MINIMAX: {
                // 2.3 MiniMax：H3 走 v2 接口，其余走海螺
                if (AiVideoUtils.isMiniMaxH3(model)) {
                    MiniMaxH3VideoRequest request = new MiniMaxH3VideoRequest().setModel(model).setPrompt(video.getPrompt())
                            .setResolution(video.getResolution()).setDuration(video.getDuration())
                            .setRatio(video.getRatio()).setFirstFrameImage(firstFrame).setLastFrameImage(lastFrame)
                            .setPromptExpansionMode(options.getPromptExpansionMode());
                    request.getReferenceImages().addAll(referenceImages);
                    request.getReferenceVideos().addAll(getInputs(video, AiVideoInputRoleEnum.REFERENCE_VIDEO));
                    request.getReferenceAudios().addAll(getInputs(video, AiVideoInputRoleEnum.REFERENCE_AUDIO));
                    MiniMaxH3VideoApi.buildRequestBody(request);
                    MiniMaxH3VideoApi api = AiVideoUtils.getMiniMaxH3Api(apiKey);
                    return () -> api.createTask(request);
                }
                validateInputsUnused(video, AiVideoInputRoleEnum.REFERENCE_VIDEO, AiVideoInputRoleEnum.REFERENCE_AUDIO);
                if (referenceImages.size() > 1) {
                    throw new IllegalArgumentException(StrUtil.format("模型({})的主体参考图最多 1 张", model));
                }
                MiniMaxHailuoVideoRequest request = new MiniMaxHailuoVideoRequest().setModel(model).setPrompt(video.getPrompt())
                        .setPromptOptimizer(options.getPromptExtend()).setDuration(video.getDuration())
                        .setResolution(video.getResolution()).setFirstFrameImage(firstFrame).setLastFrameImage(lastFrame)
                        .setSubjectReferenceImage(CollUtil.getFirst(referenceImages));
                MiniMaxHailuoVideoApi.buildRequestBody(request);
                MiniMaxHailuoVideoApi api = AiVideoUtils.getHailuoApi(apiKey);
                return () -> api.createTask(request);
            }
            case ALIBABA: {
                // 2.4 阿里：HappyHorse，或通义万相
                if (AiVideoUtils.isHappyHorse(model)) {
                    validateInputsUnused(video, AiVideoInputRoleEnum.REFERENCE_VIDEO, AiVideoInputRoleEnum.REFERENCE_AUDIO);
                    HappyHorseVideoRequest request = new HappyHorseVideoRequest().setModel(model).setPrompt(video.getPrompt())
                            .setFirstFrame(firstFrame).setResolution(video.getResolution()).setRatio(video.getRatio())
                            .setDuration(video.getDuration()).setWatermark(options.getWatermark()).setSeed(options.getSeed());
                    request.getReferenceImages().addAll(referenceImages);
                    HappyHorseVideoApi.buildParam(request);
                    HappyHorseVideoApi api = AiVideoUtils.getHappyHorseApi(apiKey);
                    return () -> api.createTask(request);
                }
                validateInputsUnused(video, AiVideoInputRoleEnum.REFERENCE_IMAGE, AiVideoInputRoleEnum.REFERENCE_VIDEO,
                        AiVideoInputRoleEnum.REFERENCE_AUDIO);
                TongYiWanVideoRequest request = new TongYiWanVideoRequest().setModel(model).setPrompt(video.getPrompt())
                        .setNegativePrompt(options.getNegativePrompt()).setSize(options.getSize())
                        .setResolution(video.getResolution()).setRatio(video.getRatio())
                        .setDuration(video.getDuration()).setPromptExtend(options.getPromptExtend())
                        .setWatermark(options.getWatermark()).setSeed(options.getSeed())
                        .setShotType(options.getShotType()).setAudio(options.getGenerateAudio())
                        .setFirstFrame(firstFrame).setLastFrame(lastFrame);
                TongYiWanVideoApi.buildParam(request);
                TongYiWanVideoApi api = AiVideoUtils.getTongYiWanApi(apiKey);
                return () -> api.createTask(request);
            }
            case GEMINI: {
                // 2.5 Google Veo（Vertex AI）：图片以 base64 提交，提交前下载
                AiVideoUtils.validateVeoModel(model);
                validateInputsUnused(video, AiVideoInputRoleEnum.REFERENCE_VIDEO, AiVideoInputRoleEnum.REFERENCE_AUDIO);
                VertexVeoVideoRequest request = new VertexVeoVideoRequest().setModel(model).setPrompt(video.getPrompt())
                        .setNegativePrompt(options.getNegativePrompt()).setAspectRatio(video.getRatio())
                        .setDurationSeconds(video.getDuration()).setResolution(video.getResolution())
                        .setGenerateAudio(options.getGenerateAudio()).setSeed(options.getSeed())
                        .setImage(AiVideoUtils.buildVeoImage(firstFrame)).setLastFrame(AiVideoUtils.buildVeoImage(lastFrame));
                referenceImages.forEach(url -> request.getReferenceImages().add(AiVideoUtils.buildVeoImage(url)));
                VertexVeoVideoApi.buildRequestBody(request);
                VertexVeoVideoApi api = AiVideoUtils.getVeoApi(apiKey);
                return () -> api.createTask(request);
            }
            default:
                throw new IllegalArgumentException(StrUtil.format("平台({})暂不支持视频生成", platform.getName()));
        }
    }

    @Override
    public AiVideoTaskResult getVideoTask(AiVideoDO video, AiApiKeyDO apiKey) {
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(video.getPlatform());
        String model = video.getModel();
        String taskId = video.getTaskId();
        switch (platform) {
            case BYTEDANCE: {
                if (AiVideoUtils.isJimeng(video)) {
                    // 即梦：model 为 req_key；只有 OmniHuman 1.5 使用 CVGetResult，其余都是 CVSync2AsyncGetResult（1.5 提交时已拦截）
                    return AiVideoUtils.getJimengApi(apiKey).getTask(new AiVideoTaskRef(JimengVisualApi.PLATFORM, model, taskId,
                            JimengVisualApi.ACTION_SYNC2ASYNC_GET_RESULT, null));
                }
                return AiVideoUtils.getSeedanceApi(apiKey).getTask(taskId);
            }
            case KLING: {
                // 可灵：查询路径依赖创建时的路由，按模型与是否有首帧推导
                boolean i2v = CollUtil.isNotEmpty(getInputs(video, AiVideoInputRoleEnum.FIRST_FRAME, false));
                String route = AiVideoUtils.isKlingLegacy(model)
                        ? (i2v ? KlingVideoApi.ROUTE_LEGACY_IMAGE2VIDEO : KlingVideoApi.ROUTE_LEGACY_TEXT2VIDEO)
                        : (i2v ? KlingVideoApi.ROUTE_IMAGE_TO_VIDEO : KlingVideoApi.ROUTE_TEXT_TO_VIDEO);
                return AiVideoUtils.getKlingApi(apiKey).getTask(new AiVideoTaskRef(KlingVideoApi.PLATFORM, model, taskId, route, null));
            }
            case MINIMAX: {
                if (AiVideoUtils.isMiniMaxH3(model)) {
                    return AiVideoUtils.getMiniMaxH3Api(apiKey).getTask(taskId);
                }
                return AiVideoUtils.getHailuoApi(apiKey).getTask(taskId);
            }
            case ALIBABA: {
                if (AiVideoUtils.isHappyHorse(model)) {
                    return AiVideoUtils.getHappyHorseApi(apiKey).getTask(taskId);
                }
                return AiVideoUtils.getTongYiWanApi(apiKey).getTask(taskId);
            }
            case GEMINI: {
                AiVideoUtils.validateVeoModel(model);
                return AiVideoUtils.getVeoApi(apiKey).getTask(new AiVideoTaskRef(VertexVeoVideoApi.PLATFORM, model, taskId, null, null));
            }
            default:
                throw new IllegalArgumentException(StrUtil.format("平台({})暂不支持视频生成", platform.getName()));
        }
    }

    // ========== 服务商：输入文件 ==========

    /**
     * 获得某个角色的第一个输入文件：私有存储时转为预签名地址，供服务商下载
     */
    private String getInput(AiVideoDO video, AiVideoInputRoleEnum role) {
        return CollUtil.getFirst(getInputs(video, role));
    }

    /**
     * 校验没有客户端不会提交的输入文件：避免被静默丢弃（例如短剧按是否通过校验，决定带哪些参考素材）；与前端 video.ts 的模型能力一致
     */
    private void validateInputsUnused(AiVideoDO video, AiVideoInputRoleEnum... roles) {
        for (AiVideoInputRoleEnum role : roles) {
            if (!getInputs(video, role, false).isEmpty()) {
                throw new IllegalArgumentException(StrUtil.format("模型({})不支持{}", video.getModel(), role.getName()));
            }
        }
    }

    /**
     * 获得某个角色的全部输入文件：私有存储时转为预签名地址，供服务商下载
     */
    private List<String> getInputs(AiVideoDO video, AiVideoInputRoleEnum role) {
        return getInputs(video, role, true);
    }

    /**
     * 获得某个角色的全部输入文件，去掉空地址
     *
     * @param presign 是否转为预签名地址：提交服务商时需要；只判断有无时（例如可灵查询的路由）不需要
     */
    private List<String> getInputs(AiVideoDO video, AiVideoInputRoleEnum role, boolean presign) {
        if (video.getInputUrls() == null) {
            return Collections.emptyList();
        }
        List<String> urls = CollUtil.removeBlank(CollUtil.newArrayList(
                video.getInputUrls().getOrDefault(role.getRole(), Collections.emptyList())));
        return presign ? convertList(urls, fileApi::presignGetUrlQuietly) : urls;
    }

    // ========== 同步 ==========

    @Override
    public int syncVideo() {
        int count = 0;
        // 1. 查询服务商并转存：各步互相独立，异常不影响后续步骤
        try {
            count += fetchVideo();
        } catch (Exception e) {
            log.error("[syncVideo][查询服务商异常]", e);
        }
        // 2. 超时：超过时限未完成的，置为已失败
        try {
            count += timeoutVideo();
        } catch (Exception e) {
            log.error("[syncVideo][超时处理异常]", e);
        }
        return count;
    }

    /**
     * 流程 2、3. 查询并转存：生成中、转存中的任务，逐个查询服务商；单个异常时保持原状态，下轮重试
     *
     * @return 处理的数量
     */
    @VisibleForTesting
    int fetchVideo() {
        // 1. 查询生成中、转存中的任务
        List<AiVideoDO> list = videoMapper.selectListByStatus(Arrays.asList(
                AiVideoStatusEnum.IN_PROGRESS.getStatus(), AiVideoStatusEnum.SAVING.getStatus()), SYNC_LIMIT);
        // 2. 逐个查询服务商：同一密钥只查一次
        Map<Long, AiApiKeyDO> apiKeyMap = new HashMap<>();
        int count = 0;
        for (AiVideoDO video : list) {
            try {
                AiApiKeyDO apiKey = apiKeyMap.computeIfAbsent(video.getApiKeyId(), apiKeyService::getApiKey);
                if (fetchVideo(video, apiKey)) {
                    count++;
                }
            } catch (Exception e) {
                log.warn("[fetchVideo][video({}) task({}) 查询或转存失败，下轮重试]", video.getId(), video.getTaskId(), e);
            }
        }
        return count;
    }

    /**
     * 查询一个任务：服务商的状态只在这里映射，不写入本地 status
     *
     * @return 是否有状态变化
     */
    private boolean fetchVideo(AiVideoDO video, AiApiKeyDO apiKey) {
        // 1. 密钥只停用也照常查询；已删除则置为已失败
        if (apiKey == null) {
            updateVideoFail(video.getId(), video.getStatus(), "渠道密钥已删除");
            return true;
        }
        // 2. 查询服务商：未结束的保持原状态
        AiVideoTaskResult result = getVideoTask(video, apiKey);
        if (!result.isFinished()) {
            return false;
        }
        // 3. 失败、取消、过期：置为已失败
        if (!result.isSucceeded()) {
            updateVideoFail(video.getId(), video.getStatus(), StrUtil.blankToDefault(result.getErrorMessage(), "生成失败"));
            return true;
        }
        // 4. 成功：先更新为转存中，再转存
        if (Objects.equals(video.getStatus(), AiVideoStatusEnum.IN_PROGRESS.getStatus())) {
            int updated = videoMapper.updateByIdAndStatus(video.getId(), AiVideoStatusEnum.IN_PROGRESS.getStatus(),
                    new AiVideoDO().setStatus(AiVideoStatusEnum.SAVING.getStatus()));
            if (updated == 0) {
                return false;
            }
        }
        transferVideo(video, result);
        return true;
    }

    /**
     * 转存：把服务商返回的视频保存到文件服务，更新为已完成。失败时抛出异常，保持转存中
     */
    private void transferVideo(AiVideoDO video, AiVideoTaskResult result) {
        // 1. 找到第一个有地址或内容的视频
        AiVideoOutput output = CollUtil.findOne(result.getVideos(),
                item -> StrUtil.isNotBlank(item.getUrl()) || item.getData() != null);
        if (output == null) {
            updateVideoFail(video.getId(), AiVideoStatusEnum.SAVING.getStatus(), "服务商返回成功，但没有视频结果");
            return;
        }
        // 2. 转存到文件服务
        byte[] content = output.getData() != null ? output.getData() : HttpUtil.downloadBytes(output.getUrl());
        String videoUrl = fileApi.createFile(content);
        // 3. 更新为已完成：仍为转存中时才更新；未指定时长的，取服务商返回值
        Integer duration = video.getDuration() == null && output.getDurationSeconds() != null
                ? (int) Math.round(output.getDurationSeconds()) : null;
        videoMapper.updateByIdAndStatus(video.getId(), AiVideoStatusEnum.SAVING.getStatus(), new AiVideoDO()
                .setVideoUrl(videoUrl).setDuration(duration).setProgress(100)
                .setStatus(AiVideoStatusEnum.SUCCESS.getStatus()).setFinishTime(LocalDateTime.now()));
    }

    /**
     * 流程 4. 超时：待处理、生成中、转存中的，从创建时间起超过 {@link #TIMEOUT_MINUTES} 分钟，置为已失败
     *
     * @return 处理的数量
     */
    @VisibleForTesting
    int timeoutVideo() {
        List<AiVideoDO> list = videoMapper.selectListByStatusAndCreateTimeLt(
                Arrays.asList(AiVideoStatusEnum.PENDING.getStatus(), AiVideoStatusEnum.IN_PROGRESS.getStatus(),
                        AiVideoStatusEnum.SAVING.getStatus()),
                LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES));
        list.forEach(video -> updateVideoFail(video.getId(), video.getStatus(), "任务超时"));
        return list.size();
    }

    /**
     * 更新为已失败：仍为 whereStatus 时才更新，避免覆盖其它执行者的结果
     */
    private void updateVideoFail(Long id, Integer whereStatus, String errorMessage) {
        videoMapper.updateByIdAndStatus(id, whereStatus, new AiVideoDO()
                .setStatus(AiVideoStatusEnum.FAIL.getStatus()).setFinishTime(LocalDateTime.now())
                .setErrorMessage(errorMessage));
    }

}
