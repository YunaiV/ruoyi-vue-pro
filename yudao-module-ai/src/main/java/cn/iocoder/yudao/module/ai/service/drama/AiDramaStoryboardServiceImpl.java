package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaStoryboardMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaVideoSubmissionMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaProjectStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaVisualAssetKindEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoModeEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoOutput;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.video.AiVideoService;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧分镜 Service 实现类
 *
 * 镜头视频复用 AI 视频的客户端（{@link cn.iocoder.yudao.module.ai.service.video.AiVideoService}），
 * 任务编号、状态、结果都存在分镜上，不写 ai_video；关键帧存在视觉资产（shot_frame 类型）中
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiDramaStoryboardServiceImpl implements AiDramaStoryboardService {

    /**
     * 每轮同步的最大数量
     */
    private static final int SYNC_LIMIT = 100;
    /**
     * 镜头未设置时长时，素材渲染的默认时长（秒）
     */
    private static final int DEFAULT_DURATION = 5;
    /**
     * 关键帧参考模式：纯文字重画，不传参考图
     */
    private static final String KEYFRAME_REFERENCE_MODE_TEXT_ONLY = "text_only_candidate";
    /**
     * 视频参考模式：连续多镜段，起始帧与角色形象一起作为参考图
     */
    private static final String VIDEO_REFERENCE_MODE_MULTI_SHOT = "multi_shot";
    /**
     * 批量生成关键帧时，一次最多的镜头数
     */
    private static final int FRAME_BATCH_MAX = 40;
    /**
     * 导入的镜头视频，边长的上限
     */
    private static final int IMPORT_VIDEO_MAX_SIDE = 3840;
    /**
     * 摄影规则中写入提示词的项：key 为字段，value 为名称
     */
    private static final Map<String, String> PHOTOGRAPHY_LABELS = new LinkedHashMap<>();

    static {
        PHOTOGRAPHY_LABELS.put("lighting", "光线");
        PHOTOGRAPHY_LABELS.put("characters", "人物");
        PHOTOGRAPHY_LABELS.put("depthOfField", "景深");
        PHOTOGRAPHY_LABELS.put("colorTone", "色调");
    }

    @Value("${yudao.ai.drama.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;
    @Value("${yudao.ai.drama.ffprobe-path:ffprobe}")
    private String ffprobePath;

    @Resource
    private AiDramaStoryboardMapper storyboardMapper;
    @Resource
    private AiDramaVideoSubmissionMapper videoSubmissionMapper;

    @Resource
    private AiDramaProjectService projectService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiDramaScriptService scriptService;
    @Resource
    private AiDramaAssetService assetService;
    @Resource
    private AiVideoService videoService;
    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private FileApi fileApi;

    // ========== 分镜 ==========

    @Override
    public List<AiDramaStoryboardDO> getStoryboardList(Long projectId) {
        return storyboardMapper.selectListByProjectId(projectId);
    }

    @Override
    public List<AiDramaStoryboardDO> getStoryboardListByProjectIds(Collection<Long> projectIds) {
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        return storyboardMapper.selectListByProjectIds(projectIds);
    }

    @Override
    public AiDramaStoryboardDO getStoryboard(Long id) {
        return storyboardMapper.selectById(id);
    }

    @Override
    public AiDramaStoryboardDO getStoryboardMy(Long userId, Long id) {
        AiDramaStoryboardDO storyboard = storyboardMapper.selectById(id);
        if (storyboard == null) {
            return null;
        }
        projectService.validateProjectMy(storyboard.getProjectId(), userId);
        return storyboard;
    }

    @Override
    public List<AiDramaStoryboardDO> getStoryboardListByScriptIdMy(Long userId, Long scriptId) {
        // 1. 校验剧本存在，且属于该用户的项目
        AiDramaScriptDO script = scriptService.getScript(scriptId);
        if (script == null) {
            throw exception(DRAMA_SCRIPT_NOT_EXISTS);
        }
        projectService.validateProjectMy(script.getProjectId(), userId);

        // 2. 该剧本版本的分镜
        return filterList(storyboardMapper.selectListByProjectId(script.getProjectId()),
                storyboard -> Objects.equals(storyboard.getScriptId(), scriptId));
    }

    @Override
    public void validateStoryboardVideoNotRunning(Long id) {
        AiDramaStoryboardDO storyboard = storyboardMapper.selectById(id);
        if (storyboard != null) {
            validateVideoNotRunning(storyboard);
        }
    }

    @Override
    public Long createStoryboardMy(Long userId, AiDramaStoryboardSaveReqVO createReqVO) {
        // 1. 校验项目
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);

        // 2.1 查询：所属的剧本版本，未指定时为项目当前的剧本，指定时需属于该项目；未指定场次时，排在该版本的最后
        Long scriptId = createReqVO.getScriptId();
        if (scriptId == null) {
            AiDramaScriptDO script = scriptService.getScriptByProjectId(createReqVO.getProjectId());
            scriptId = script != null ? script.getId() : null;
        } else {
            AiDramaScriptDO script = scriptService.getScript(scriptId);
            if (script == null || ObjUtil.notEqual(script.getProjectId(), createReqVO.getProjectId())) {
                throw exception(DRAMA_SCRIPT_NOT_EXISTS);
            }
        }
        Integer sceneNo = createReqVO.getSceneNo();
        if (sceneNo == null) {
            Long finalScriptId = scriptId;
            List<AiDramaStoryboardDO> storyboards = filterList(storyboardMapper.selectListByProjectId(createReqVO.getProjectId()),
                    item -> Objects.equals(item.getScriptId(), finalScriptId));
            sceneNo = ObjUtil.defaultIfNull(getMaxValue(storyboards, item -> ObjUtil.defaultIfNull(item.getSceneNo(), 0)), 0) + 1;
        } else {
            validateSceneNoUnique(createReqVO.getProjectId(), scriptId, sceneNo, null);
        }
        // 2.2 插入
        AiDramaStoryboardDO storyboard = BeanUtils.toBean(createReqVO, AiDramaStoryboardDO.class)
                .setId(null).setScriptId(scriptId).setSceneNo(sceneNo);
        storyboardMapper.insert(storyboard);
        return storyboard.getId();
    }

    @Override
    public void updateStoryboardMy(Long userId, AiDramaStoryboardSaveReqVO updateReqVO) {
        // 1. 校验存在，且镜头视频不在生成中；镜头序号在该剧本版本中不重复
        AiDramaStoryboardDO storyboard = validateStoryboardMy(updateReqVO.getId(), userId);
        validateVideoNotRunning(storyboard);
        validateSceneNoUnique(storyboard.getProjectId(), storyboard.getScriptId(), updateReqVO.getSceneNo(), storyboard.getId());

        // 2. 更新：不修改所属项目、剧本；清空的场景、道具一并清空
        AiDramaStoryboardDO updateObj = BeanUtils.toBean(updateReqVO, AiDramaStoryboardDO.class,
                o -> o.setProjectId(null).setScriptId(null));
        storyboardMapper.updateForSave(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStoryboardMy(Long userId, Long id) {
        // 校验存在，且镜头视频不在生成中
        AiDramaStoryboardDO storyboard = validateStoryboardMy(id, userId);
        validateVideoNotRunning(storyboard);
        // 删除：关键帧、视频提交记录一并删除
        assetService.deleteStoryboardFrameList(Collections.singletonList(id));
        videoSubmissionMapper.deleteByStoryboardIds(Collections.singletonList(id));
        storyboardMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceStoryboardList(Long projectId, Long scriptId, List<AiDramaStoryboardDO> storyboards) {
        // 删除该剧本版本原来的分镜，以及它们的关键帧、视频提交记录
        List<Long> oldIds = convertList(storyboardMapper.selectListByProjectId(projectId), AiDramaStoryboardDO::getId,
                item -> Objects.equals(item.getScriptId(), scriptId));
        assetService.deleteStoryboardFrameList(oldIds);
        if (CollUtil.isNotEmpty(oldIds)) {
            videoSubmissionMapper.deleteByStoryboardIds(oldIds);
        }
        storyboardMapper.deleteByProjectIdAndScriptId(projectId, scriptId);
        // 插入新的分镜
        storyboardMapper.insertBatch(storyboards);
    }

    @Override
    public void updateStoryboardList(List<AiDramaStoryboardDO> updateList) {
        storyboardMapper.updateBatch(updateList);
    }

    @Override
    public void deleteStoryboardListByProjectId(Long projectId) {
        videoSubmissionMapper.deleteByProjectId(projectId);
        storyboardMapper.deleteByProjectId(projectId);
    }

    /**
     * 校验分镜存在，且属于该用户的项目
     */
    private AiDramaStoryboardDO validateStoryboardMy(Long id, Long userId) {
        AiDramaStoryboardDO storyboard = storyboardMapper.selectById(id);
        if (storyboard == null) {
            throw exception(DRAMA_STORYBOARD_NOT_EXISTS);
        }
        projectService.validateProjectMy(storyboard.getProjectId(), userId);
        return storyboard;
    }

    /**
     * 校验镜头序号在同一剧本版本中不重复：序号为空时不校验
     *
     * @param id 分镜编号：修改时排除自身，新增时为空
     */
    private void validateSceneNoUnique(Long projectId, Long scriptId, Integer sceneNo, Long id) {
        if (sceneNo == null) {
            return;
        }
        if (CollUtil.contains(storyboardMapper.selectListByProjectId(projectId), item -> Objects.equals(item.getScriptId(), scriptId)
                && Objects.equals(item.getSceneNo(), sceneNo) && ObjUtil.notEqual(item.getId(), id))) {
            throw invalidParamException("镜头序号 {} 已存在", sceneNo);
        }
    }

    /**
     * 校验镜头视频不在生成中：待处理、生成中、转存中
     */
    private static void validateVideoNotRunning(AiDramaStoryboardDO storyboard) {
        if (AiDramaStatusEnum.isRunning(storyboard.getVideoStatus())) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    /**
     * 校验剧本未修改：剧本修改后，需要重新拆分分镜，才能生成关键帧、镜头视频
     */
    private static void validateScriptNotChanged(AiDramaProjectDO project) {
        if (ObjUtil.equal(project.getStatus(), AiDramaProjectStatusEnum.SCRIPT_CHANGED.getStatus())) {
            throw exception(DRAMA_SCRIPT_CHANGED);
        }
    }

    // ========== 镜头视频：提交 ==========

    @Override
    public void generateStoryboardVideoMy(Long userId, AiDramaStoryboardVideoReqVO reqVO) {
        // 1. 校验分镜、视频模型与其当前密钥：镜头设置了视频模型时，使用镜头的模型、分辨率
        AiDramaStoryboardDO storyboard = validateStoryboardMy(reqVO.getId(), userId);
        Long modelId = ObjUtil.defaultIfNull(storyboard.getVideoModelId(), reqVO.getModelId());
        String resolution = storyboard.getVideoModelId() != null ? storyboard.getVideoResolution() : reqVO.getResolution();
        AiModelDO model = videoService.validateVideoModel(modelId);
        AiApiKeyDO apiKey = videoService.validateVideoApiKey(model);

        // 2. 提交：带上前端的请求编号，重复提交时不再提交服务商
        submitStoryboardVideo(storyboard, model, apiKey, resolution, reqVO.getRequestId());
    }

    @Override
    public int generateStoryboardVideoBatchMy(Long userId, AiDramaStoryboardVideoReqVO reqVO) {
        // 1. 校验项目（剧本未修改）、所选视频模型与其当前密钥；剧本版本未指定时为当前剧本
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getId(), userId);
        validateScriptNotChanged(project);
        AiModelDO defaultModel = videoService.validateVideoModel(reqVO.getModelId());
        AiApiKeyDO defaultApiKey = videoService.validateVideoApiKey(defaultModel);
        Long scriptId = reqVO.getScriptId();
        if (scriptId == null) {
            AiDramaScriptDO script = scriptService.getScriptByProjectId(reqVO.getId());
            scriptId = script != null ? script.getId() : null;
        }

        // 2. 逐个提交：只处理该剧本版本中未生成、已失败的；镜头设置了视频模型时，使用镜头的模型、分辨率
        int count = 0;
        for (AiDramaStoryboardDO storyboard : storyboardMapper.selectListByProjectId(reqVO.getId())) {
            if (ObjUtil.notEqual(storyboard.getScriptId(), scriptId) || (storyboard.getVideoStatus() != null
                    && ObjUtil.notEqual(storyboard.getVideoStatus(), AiDramaStatusEnum.FAIL.getStatus()))) {
                continue;
            }
            Long modelId = ObjUtil.defaultIfNull(storyboard.getVideoModelId(), reqVO.getModelId());
            String resolution = storyboard.getVideoModelId() != null ? storyboard.getVideoResolution() : reqVO.getResolution();
            try {
                AiModelDO model = Objects.equals(modelId, defaultModel.getId()) ? defaultModel : videoService.validateVideoModel(modelId);
                AiApiKeyDO apiKey = model == defaultModel ? defaultApiKey : videoService.validateVideoApiKey(model);
                submitStoryboardVideo(storyboard, model, apiKey, resolution, null);
            } catch (ServiceException e) {
                // 镜头的模型不可用、参数不通过（例如时长不被模型支持）：记为已失败，继续下一个
                storyboardMapper.updateById(new AiDramaStoryboardDO().setId(storyboard.getId())
                        .setVideoModelId(modelId).setVideoResolution(StrUtil.nullToEmpty(resolution))
                        .setVideoStatus(AiDramaStatusEnum.FAIL.getStatus())
                        .setErrorMessage(e.getMessage()));
            }
            count++;
        }
        return count;
    }

    @Override
    public void resubmitStoryboardVideo(Long id) {
        // 1.1 校验已失败，且记录了上次的视频模型
        AiDramaStoryboardDO storyboard = storyboardMapper.selectById(id);
        if (storyboard == null) {
            throw exception(DRAMA_STORYBOARD_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(storyboard.getVideoStatus(), AiDramaStatusEnum.FAIL.getStatus())) {
            throw exception(DRAMA_TASK_NOT_FAIL);
        }
        if (storyboard.getVideoModelId() == null) {
            throw exception(DRAMA_TASK_RESUBMIT_NO_MODEL);
        }
        // 1.2 校验视频模型与其当前密钥
        AiModelDO model = videoService.validateVideoModel(storyboard.getVideoModelId());
        AiApiKeyDO apiKey = videoService.validateVideoApiKey(model);

        // 2. 提交：沿用上次的分辨率
        submitStoryboardVideo(storyboard, model, apiKey, storyboard.getVideoResolution(), null);
    }

    /**
     * 提交镜头视频：本地校验通过后记录提交、置为待处理，再提交服务商
     *
     * 1. 待处理时分镜的任务编号先记为请求编号，提交后的回写以它为条件：提交期间被标记失败又重新提交时，不覆盖新一轮
     * 2. 同一请求编号重复提交（例如前端网络重试）时，参数一致直接返回，不再提交服务商；参数不一致时拒绝
     * 3. 提交明确失败时，提交记录为已失败；结果未知（超时、网络中断、5xx）时保持待处理，由用户核对后确认任务编号或确认失败
     *
     * @param requestId 请求编号：为空时生成
     */
    private void submitStoryboardVideo(AiDramaStoryboardDO storyboard, AiModelDO model, AiApiKeyDO apiKey, String resolution,
                                       String requestId) {
        // 1.1 构建并在本地校验请求：不通过直接报错，不修改记录；分辨率为空串时与不传一致
        AiDramaProjectDO project = projectService.validateProjectExists(storyboard.getProjectId());
        validateScriptNotChanged(project);
        VideoSubmitter submitter = buildVideoSubmitter(storyboard, project, model, apiKey, StrUtil.emptyToNull(resolution));
        String parametersHash = buildParametersHash(submitter.getVideo());
        // 1.2 同一请求编号已提交过：参数一致视为重复提交
        if (requestId != null) {
            AiDramaVideoSubmissionDO submission = videoSubmissionMapper.selectByRequestId(requestId);
            if (submission != null) {
                if (ObjUtil.notEqual(submission.getStoryboardId(), storyboard.getId())
                        || ObjUtil.notEqual(submission.getParametersHash(), parametersHash)) {
                    throw exception(DRAMA_VIDEO_SUBMISSION_PARAMS_CHANGED);
                }
                return;
            }
        } else {
            requestId = IdUtil.fastSimpleUUID();
        }
        // 1.3 校验镜头视频不在生成中
        validateVideoNotRunning(storyboard);

        // 2.1 记录提交：参数哈希、模型快照、提交前的视频
        Integer whereStatus = AiDramaStatusEnum.PENDING.getStatus();
        AiDramaVideoSubmissionDO submission = new AiDramaVideoSubmissionDO().setRequestId(requestId)
                .setProjectId(storyboard.getProjectId()).setStoryboardId(storyboard.getId()).setParametersHash(parametersHash)
                .setModelId(model.getId()).setModel(model.getModel())
                .setFirstFrameUrl(getSubmittedFirstFrameUrl(submitter.getVideo())).setStatus(whereStatus)
                .setPreviousVideo(new AiDramaVideoSubmissionDO.VideoSnapshot().setTaskId(storyboard.getTaskId())
                        .setVideoStatus(storyboard.getVideoStatus()).setVideoUrl(storyboard.getVideoUrl())
                        .setLastFrameUrl(storyboard.getLastFrameUrl()));
        videoSubmissionMapper.insert(submission);
        // 2.2 置为待处理（记录视频模型、分辨率，供重新提交沿用；分辨率为空时记为空串，覆盖上次的）
        storyboardMapper.updateById(new AiDramaStoryboardDO().setId(storyboard.getId())
                .setVideoModelId(model.getId()).setVideoModel(model.getModel())
                .setVideoResolution(StrUtil.nullToEmpty(resolution))
                .setTaskId(requestId).setVideoStatus(whereStatus));

        // 3. 提交服务商
        try {
            AiVideoTaskRef ref = submitter.getSubmitter().get();
            storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(), whereStatus, requestId,
                    new AiDramaStoryboardDO().setTaskId(ref.getTaskId()).setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));
            videoSubmissionMapper.updateByIdAndStatus(submission.getId(), whereStatus, new AiDramaVideoSubmissionDO()
                    .setTaskId(ref.getTaskId()).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));
        } catch (Exception e) {
            log.error("[submitStoryboardVideo][storyboard({}) request({}) 提交服务商失败]", storyboard.getId(), requestId, e);
            String errorMessage = AiVideoException.buildSubmitErrorMessage(e);
            updateVideoFail(storyboard.getId(), whereStatus, requestId, errorMessage);
            if (!(e instanceof AiVideoException && ((AiVideoException) e).isSubmitOutcomeUnknown())) {
                videoSubmissionMapper.updateByIdAndStatus(submission.getId(), whereStatus, new AiDramaVideoSubmissionDO()
                        .setStatus(AiDramaStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage));
            }
        }
    }

    /**
     * 计算提交参数的哈希：视频记录去掉任务编号后的 JSON 的 SHA-256
     */
    private static String buildParametersHash(AiVideoDO video) {
        return DigestUtil.sha256Hex(JsonUtils.toJsonString(BeanUtils.toBean(video, AiVideoDO.class).setTaskId(null)));
    }

    /**
     * 获得视频记录中实际提交的首帧：图生视频时有值，参考生视频、文生视频时为空
     */
    private static String getSubmittedFirstFrameUrl(AiVideoDO video) {
        return video.getInputUrls() != null
                ? CollUtil.getFirst(video.getInputUrls().get(AiVideoInputRoleEnum.FIRST_FRAME.getRole())) : null;
    }

    /**
     * 镜头视频的提交方法，以及对应的视频记录（不入库，用于计算参数哈希）
     */
    @Data
    @AllArgsConstructor
    private static class VideoSubmitter {

        private AiVideoDO video;
        private Supplier<AiVideoTaskRef> submitter;

    }

    /**
     * 构建提交服务商的方法：使用起始帧时为图生视频；不使用时，带上参考素材为参考生视频，模型不支持时与原来一样为文生视频
     *
     * 参考素材是否支持，由各平台客户端的本地校验判断：不通过时依次去掉参考音频、动作参考视频再试，仍不通过则不带参考素材
     *
     * 连续多镜段：起始帧作为第 1 张参考图，与角色形象一起为参考生视频；每个出场角色都需要参考图，模型不支持参考图时不能生成
     */
    private VideoSubmitter buildVideoSubmitter(AiDramaStoryboardDO storyboard, AiDramaProjectDO project,
                                               AiModelDO model, AiApiKeyDO apiKey, String resolution) {
        String prompt = buildStoryboardPrompt(storyboard, project, false);
        String firstFrameUrl = getFirstFrameUrl(storyboard, project);
        boolean multiShot = isMultiShot(storyboard);
        // 1. 不使用起始帧、或连续多镜段：依次尝试 参考图 + 参考音频 + 参考视频、去掉参考音频、再去掉参考视频；
        // 单镜使用起始帧时不带，各平台的首帧都不能与参考素材混用
        if (firstFrameUrl == null || multiShot) {
            Map<Long, AiDramaCharacterDO> characterMap = convertMap(assetService.getCharacterList(project.getId()),
                    AiDramaCharacterDO::getId);
            Map<String, Set<String>> images = new LinkedHashMap<>();
            if (firstFrameUrl != null) {
                putIfNotBlank(images, firstFrameUrl, "起始画面");
            }
            getReferenceImages(storyboard, project, characterMap, false).forEach((url, names) ->
                    images.computeIfAbsent(url, key -> new LinkedHashSet<>()).addAll(names));
            if (multiShot) {
                validateMultiShotCharacterImages(storyboard, characterMap);
            }
            Map<String, Set<String>> audios = getReferenceAudios(storyboard, project, characterMap);
            List<String> videos = storyboard.getContinuity() != null ? CollUtil.distinct(filterList(
                    CollUtil.emptyIfNull(storyboard.getContinuity().getReferenceVideoUrls()), StrUtil::isNotBlank))
                    : Collections.emptyList();
            List<ReferenceInputs> attempts = new ArrayList<>();
            attempts.add(new ReferenceInputs(images, audios, videos));
            attempts.add(new ReferenceInputs(images, Collections.emptyMap(), videos));
            attempts.add(new ReferenceInputs(images, Collections.emptyMap(), Collections.emptyList()));
            Set<ReferenceInputs> tried = new HashSet<>();
            for (ReferenceInputs inputs : attempts) {
                if (inputs.isEmpty() || !tried.add(inputs)) {
                    continue;
                }
                VideoSubmitter submitter = buildReferenceVideoSubmitter(storyboard, project, model, apiKey,
                        prompt, resolution, inputs);
                if (submitter != null) {
                    return submitter;
                }
            }
            // 连续多镜段有参考图（起始帧、角色形象等）时，不退回图生视频、文生视频，避免丢掉必需的角色参考
            if (multiShot && !images.isEmpty()) {
                throw invalidParamException("视频模型不支持参考图，不能生成连续多镜段");
            }
        }
        // 2. 有起始帧为图生视频，否则为文生视频；起始帧按视频参考图镜像替换
        String submitFirstFrameUrl = firstFrameUrl != null
                ? applyReferenceMirrors(storyboard, Collections.singletonList(firstFrameUrl)).get(0) : null;
        AiVideoDO video = buildVideo(storyboard, model, prompt, submitFirstFrameUrl, project.getComposeAspectRatio())
                .setResolution(resolution);
        return new VideoSubmitter(video, videoService.buildVideoSubmitter(video, apiKey));
    }

    /**
     * 校验连续多镜段的每个出场角色都有参考图：缺少时视频无法对应角色的身份与衣装
     */
    private void validateMultiShotCharacterImages(AiDramaStoryboardDO storyboard, Map<Long, AiDramaCharacterDO> characterMap) {
        Map<Long, String> imageUrls = getAppearanceImageUrls(storyboard, characterMap);
        assetService.getAppearanceList(getAppearanceIds(storyboard)).forEach(appearance -> {
            AiDramaCharacterDO character = characterMap.get(appearance.getCharacterId());
            if (character != null && StrUtil.isBlank(imageUrls.get(appearance.getId()))) {
                throw invalidParamException("连续多镜段缺少角色参考：{}", character.getName());
            }
        });
    }

    /**
     * 获得出场角色形象的参考图，缺图时依次回退：该形象的选用图、形象参考图 → 同角色其它有图的形象（按形象序号，主形象优先）→ 角色参考图；
     * 关键帧、视频共用，只使用本项目角色的
     *
     * @return key 为形象编号，value 为参考图地址；都没有图时不包含
     */
    private Map<Long, String> getAppearanceImageUrls(AiDramaStoryboardDO storyboard, Map<Long, AiDramaCharacterDO> characterMap) {
        // 1.1 出场形象：只取本项目角色的
        List<AiDramaCharacterAppearanceDO> appearances = filterList(assetService.getAppearanceList(getAppearanceIds(storyboard)),
                appearance -> characterMap.containsKey(appearance.getCharacterId()));
        if (appearances.isEmpty()) {
            return Collections.emptyMap();
        }
        // 1.2 一次查询出场角色的全部形象
        List<AiDramaCharacterAppearanceDO> characterAppearances = assetService.getAppearanceListByCharacterIds(
                convertSet(appearances, AiDramaCharacterAppearanceDO::getCharacterId));
        // 1.3 按形象序号排序（主形象优先，没有序号的最后）后，按角色分组
        Map<Long, List<AiDramaCharacterAppearanceDO>> characterAppearanceMap = convertMultiMap(characterAppearances.stream()
                .sorted(Comparator.comparing(AiDramaCharacterAppearanceDO::getAppearanceIndex,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList()), AiDramaCharacterAppearanceDO::getCharacterId);

        // 2. 逐个出场形象回退取图
        Map<Long, String> result = new HashMap<>();
        for (AiDramaCharacterAppearanceDO appearance : appearances) {
            // 2.1 该形象自身的图
            String url = getAppearanceImageUrl(appearance);
            // 2.2 同角色第一个有图的形象
            if (StrUtil.isBlank(url)) {
                url = findFirst(convertList(characterAppearanceMap.get(appearance.getCharacterId()),
                        AiDramaStoryboardServiceImpl::getAppearanceImageUrl), StrUtil::isNotBlank);
            }
            // 2.3 角色参考图
            if (StrUtil.isBlank(url)) {
                url = characterMap.get(appearance.getCharacterId()).getReferenceImageUrl();
            }
            if (StrUtil.isNotBlank(url)) {
                result.put(appearance.getId(), url);
            }
        }
        return result;
    }

    /**
     * 获得形象自身的图：选用的候选图，没有时为形象参考图
     */
    private static String getAppearanceImageUrl(AiDramaCharacterAppearanceDO appearance) {
        String selectedUrl = getSelectedUrl(appearance.getImageUrls(), appearance.getSelectedIndex());
        return StrUtil.blankToDefault(selectedUrl, appearance.getReferenceImageUrl());
    }

    /**
     * 参考生视频的参考素材
     */
    @Data
    @AllArgsConstructor
    private static class ReferenceInputs {

        /**
         * 参考图：key 为地址，value 为对应的角色形象、场景、道具（可以有多个）
         */
        private Map<String, Set<String>> images;
        /**
         * 参考音频：key 为地址，value 为对应的角色名称（可以有多个）
         */
        private Map<String, Set<String>> audios;
        /**
         * 动作参考视频地址
         */
        private List<String> videos;

        boolean isEmpty() {
            return images.isEmpty() && audios.isEmpty() && videos.isEmpty();
        }

    }

    /**
     * 构建参考生视频的提交方法：模型不支持这些参考素材（本地校验不通过）时，返回 null
     */
    private VideoSubmitter buildReferenceVideoSubmitter(AiDramaStoryboardDO storyboard, AiDramaProjectDO project,
                                                        AiModelDO model, AiApiKeyDO apiKey, String prompt, String resolution,
                                                        ReferenceInputs inputs) {
        // 1.1 参考素材：按顺序编号，与提示词中的说明对应
        Map<String, List<String>> inputUrls = new LinkedHashMap<>();
        if (!inputs.getImages().isEmpty()) {
            inputUrls.put(AiVideoInputRoleEnum.REFERENCE_IMAGE.getRole(),
                    applyReferenceMirrors(storyboard, new ArrayList<>(inputs.getImages().keySet())));
        }
        if (!inputs.getAudios().isEmpty()) {
            inputUrls.put(AiVideoInputRoleEnum.REFERENCE_AUDIO.getRole(), new ArrayList<>(inputs.getAudios().keySet()));
        }
        if (!inputs.getVideos().isEmpty()) {
            inputUrls.put(AiVideoInputRoleEnum.REFERENCE_VIDEO.getRole(), new ArrayList<>(inputs.getVideos()));
        }
        // 1.2 构建视频记录：提示词补充每个参考素材对应的角色、场景、道具
        AiVideoDO video = buildVideo(storyboard, model, buildReferencePrompt(prompt, inputs), null, project.getComposeAspectRatio())
                .setMode(AiVideoModeEnum.REFERENCE.getMode()).setInputUrls(inputUrls).setResolution(resolution);

        // 2. 本地校验：不通过时返回 null，由调用方去掉参考素材再试
        try {
            return new VideoSubmitter(video, videoService.buildVideoSubmitter(video, apiKey));
        } catch (ServiceException e) {
            log.info("[buildReferenceVideoSubmitter][storyboard({}) 模型({}) 不支持参考图({} 张)、参考音频({} 个)、参考视频({} 个)：{}]",
                    storyboard.getId(), model.getModel(), inputs.getImages().size(), inputs.getAudios().size(),
                    inputs.getVideos().size(), e.getMessage());
            return null;
        }
    }

    /**
     * 拼接参考生视频的提示词：在原提示词后，说明每张参考图、每段参考音频、参考视频对应谁，多角色时让声音与发言角色对应
     *
     * 例如说：参考素材：参考图1为林夏的形象；参考图2为场景车厢；参考音频1为林夏的音色、周远的音色；参考视频1为人物动作与运镜参考
     */
    private static String buildReferencePrompt(String prompt, ReferenceInputs inputs) {
        List<String> parts = new ArrayList<>();
        int index = 1;
        for (Set<String> names : inputs.getImages().values()) {
            parts.add("参考图" + index++ + "为" + String.join("、", names));
        }
        index = 1;
        for (Set<String> names : inputs.getAudios().values()) {
            parts.add("参考音频" + index++ + "为" + String.join("、", names));
        }
        for (index = 1; index <= inputs.getVideos().size(); index++) {
            parts.add("参考视频" + index + "为人物动作与运镜参考");
        }
        return prompt + "。参考素材：" + String.join("；", parts);
    }

    /**
     * 获得参考图：出场角色形象、场景的选用图，以及本镜道具的图片；只使用本项目的资产
     *
     * @param frame 是否为关键帧：关键帧只带 0 秒出镜角色的形象
     * @return key 为图片地址，value 为对应的角色形象、场景、道具；按地址去重，共用地址的都保留
     */
    private Map<String, Set<String>> getReferenceImages(AiDramaStoryboardDO storyboard, AiDramaProjectDO project,
                                                        Map<Long, AiDramaCharacterDO> characterMap, boolean frame) {
        Map<String, Set<String>> images = new LinkedHashMap<>();
        // 角色形象：只使用本项目角色的；缺图时按 getAppearanceImageUrls 的次序回退
        Map<Long, String> appearanceImageUrls = getAppearanceImageUrls(storyboard, characterMap);
        assetService.getAppearanceList(getAppearanceIds(storyboard)).forEach(appearance -> {
            AiDramaCharacterDO character = characterMap.get(appearance.getCharacterId());
            if (character != null && !isFrameAbsent(storyboard, frame, character.getName())) {
                putIfNotBlank(images, appearanceImageUrls.get(appearance.getId()), character.getName() + "的形象");
            }
        });
        // 场景：设置为不包含场景图时不带
        AiDramaLocationDO location = getStoryboardLocation(storyboard, project);
        if (location != null && (storyboard.getContinuity() == null
                || ObjUtil.notEqual(storyboard.getContinuity().getIncludeLocation(), Boolean.FALSE))) {
            putIfNotBlank(images, getSelectedUrl(location.getImageUrls(), location.getSelectedIndex()),
                    "场景" + location.getName());
        }
        // 道具
        getStoryboardProps(storyboard, project.getId()).forEach(prop ->
                putIfNotBlank(images, prop.getImageUrl(), "道具" + prop.getTitle()));
        return images;
    }

    /**
     * 获得参考音频：发言角色的选用样音，以及本镜使用的参考音频；旁白、没有选用样音的角色不带
     *
     * @return key 为音频地址，value 为对应的说明；按地址去重，共用样音的角色都保留
     */
    private Map<String, Set<String>> getReferenceAudios(AiDramaStoryboardDO storyboard, AiDramaProjectDO project,
                                                        Map<Long, AiDramaCharacterDO> characterMap) {
        List<AiDramaAudioDO> projectAudios = assetService.getAudioList(project.getId());
        Map<String, Set<String>> audios = new LinkedHashMap<>();
        // 1. 每个发言角色的选用样音
        Map<Long, AiDramaAudioDO> audioMap = convertMap(projectAudios, AiDramaAudioDO::getId);
        getSpeakerIds(storyboard, characterMap).forEach(speakerId -> {
            AiDramaCharacterDO character = characterMap.get(speakerId);
            AiDramaAudioDO audio = character.getSampleAudioId() != null ? audioMap.get(character.getSampleAudioId()) : null;
            putIfNotBlank(audios, audio != null ? audio.getFileUrl() : null, character.getName() + "的音色");
        });
        // 2. 参考音频：设置了使用本镜的
        projectAudios.forEach(audio -> {
            if (Objects.equals(audio.getType(), AiDramaAudioTypeEnum.REFERENCE.getType())
                    && CollUtil.contains(audio.getStoryboardIds(), storyboard.getId())) {
                putIfNotBlank(audios, audio.getFileUrl(), "声音参考《" + StrUtil.blankToDefault(audio.getName(), "参考音频") + "》");
            }
        });
        return audios;
    }

    /**
     * 获得发言角色：镜头指定了的按指定的顺序，否则按台词的发言顺序；去重，只使用本项目的角色
     */
    private static Set<Long> getSpeakerIds(AiDramaStoryboardDO storyboard, Map<Long, AiDramaCharacterDO> characterMap) {
        List<Long> voiceSpeakerIds = storyboard.getContinuity() != null ? storyboard.getContinuity().getVoiceSpeakerIds() : null;
        return CollUtil.isNotEmpty(voiceSpeakerIds)
                ? new LinkedHashSet<>(filterList(voiceSpeakerIds, characterMap::containsKey))
                : new LinkedHashSet<>(convertList(storyboard.getDialogues(),
                AiDramaStoryboardDO.Dialogue::getCharacterId, dialogue -> characterMap.containsKey(dialogue.getCharacterId())));
    }

    /**
     * 是否为关键帧不出镜的角色：关键帧只画 0 秒出镜角色，后续入场的不提前画出；未规划出镜角色时不过滤
     */
    private static boolean isFrameAbsent(AiDramaStoryboardDO storyboard, boolean frame, String name) {
        List<String> presentCharacters = storyboard.getContinuity() != null
                ? storyboard.getContinuity().getPresentCharacters() : null;
        return frame && presentCharacters != null && !presentCharacters.contains(name);
    }

    /**
     * 是否为连续多镜段：一段连续表演包含多个剪辑镜头，起始帧与角色形象一起作为参考图
     */
    private static boolean isMultiShot(AiDramaStoryboardDO storyboard) {
        return storyboard.getContinuity() != null
                && VIDEO_REFERENCE_MODE_MULTI_SHOT.equals(storyboard.getContinuity().getVideoReferenceMode());
    }

    /**
     * 替换为视频参考图镜像：提交服务商时使用镜像地址，顺序不变，与提示词中的编号对应；镜像的原图不在本镜参考图中时，视为已过期
     */
    private static List<String> applyReferenceMirrors(AiDramaStoryboardDO storyboard, List<String> urls) {
        Map<String, String> mirrors = storyboard.getContinuity() != null
                ? storyboard.getContinuity().getVideoReferenceMirrors() : null;
        if (CollUtil.isEmpty(mirrors)) {
            return urls;
        }
        for (String source : mirrors.keySet()) {
            if (!urls.contains(source)) {
                throw invalidParamException("视频参考镜像已过期：原图不在本镜参考图中");
            }
        }
        return convertList(urls, url -> StrUtil.blankToDefault(mirrors.get(url), url));
    }

    private static void putIfNotBlank(Map<String, Set<String>> map, String url, String name) {
        if (StrUtil.isNotBlank(url)) {
            map.computeIfAbsent(url, key -> new LinkedHashSet<>()).add(name);
        }
    }

    /**
     * 获得选用的候选图地址：未选用时为空
     */
    private static String getSelectedUrl(List<String> imageUrls, Integer selectedIndex) {
        if (CollUtil.isEmpty(imageUrls) || selectedIndex == null || selectedIndex < 0 || selectedIndex >= imageUrls.size()) {
            return null;
        }
        return imageUrls.get(selectedIndex);
    }

    /**
     * 获得出场角色引用的形象编号
     */
    private static List<Long> getAppearanceIds(AiDramaStoryboardDO storyboard) {
        return convertList(storyboard.getCharacters(), AiDramaStoryboardDO.CharacterRef::getAppearanceId,
                ref -> ref.getAppearanceId() != null);
    }

    /**
     * 获得镜头的场景：只使用本项目的
     */
    private AiDramaLocationDO getStoryboardLocation(AiDramaStoryboardDO storyboard, AiDramaProjectDO project) {
        if (storyboard.getLocationId() == null) {
            return null;
        }
        AiDramaLocationDO location = assetService.getLocation(storyboard.getLocationId());
        return location != null && Objects.equals(location.getProjectId(), project.getId()) ? location : null;
    }

    /**
     * 获得镜头生效的道具：镜头显式指定了的（包括空数组）按指定的；未指定时，按道具关联的镜头序号匹配
     */
    private List<AiDramaVisualAssetDO> getStoryboardProps(AiDramaStoryboardDO storyboard, Long projectId) {
        if (storyboard.getVisualAssetIds() != null) {
            return filterList(assetService.getVisualAssetList(storyboard.getVisualAssetIds()), prop ->
                    Objects.equals(prop.getProjectId(), projectId) && prop.getParentId() == null
                            && Objects.equals(prop.getKind(), AiDramaVisualAssetKindEnum.PROP.getType()));
        }
        return filterList(assetService.getPropList(projectId), prop -> prop.getReferenceImages() != null
                && CollUtil.contains(prop.getReferenceImages().getShotNumbers(), storyboard.getSceneNo()));
    }

    /**
     * 构建视频记录（不入库）：用于复用 AI 视频的客户端；有起始帧时为图生视频，否则为文生视频
     *
     * 提交、查询使用同一份构建逻辑：部分平台（例如可灵）的查询路径依赖生成方式
     */
    @VisibleForTesting
    static AiVideoDO buildVideo(AiDramaStoryboardDO storyboard, AiModelDO model, String prompt, String firstFrameUrl,
                                String aspectRatio) {
        return new AiVideoDO().setType(AiVideoTypeEnum.GENERATE.getType())
                .setMode(firstFrameUrl != null ? AiVideoModeEnum.IMAGE.getMode() : AiVideoModeEnum.TEXT.getMode())
                .setPlatform(model.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                .setPrompt(prompt).setTaskId(storyboard.getTaskId())
                .setInputUrls(firstFrameUrl != null ? Collections.singletonMap(AiVideoInputRoleEnum.FIRST_FRAME.getRole(),
                        Collections.singletonList(firstFrameUrl)) : null)
                .setRatio(aspectRatio).setDuration(storyboard.getDuration());
    }

    /**
     * 获得生成视频使用的起始帧：优先手动起始帧，其次本镜可用的关键帧（出图成功、且对应当前输入）；设置为不使用、或都没有时为空
     */
    private String getFirstFrameUrl(AiDramaStoryboardDO storyboard, AiDramaProjectDO project) {
        AiDramaStoryboardDO.Continuity continuity = storyboard.getContinuity();
        if (continuity != null && Boolean.FALSE.equals(continuity.getUseFirstFrame())) {
            return null;
        }
        if (continuity != null && continuity.getManualStartFrame() != null
                && StrUtil.isNotBlank(continuity.getManualStartFrame().getUrl())) {
            return continuity.getManualStartFrame().getUrl();
        }
        AiDramaVisualAssetDO frame = assetService.getStoryboardFrame(storyboard.getId());
        return isCurrentFrame(frame, storyboard, project) ? frame.getImageUrl() : null;
    }

    /**
     * 拼接镜头提示词：画面描述、景别运镜、场景、出场角色与站位、道具、表演、台词、画风；只使用本项目的资产
     *
     * @param frame 是否为关键帧：关键帧使用图片提示词，带上画面焦点、导演指令；不带表演与台词，避免把台词画成文字
     */
    private String buildStoryboardPrompt(AiDramaStoryboardDO storyboard, AiDramaProjectDO project, boolean frame) {
        List<String> parts = new ArrayList<>();
        AiDramaStoryboardDO.Continuity continuity = storyboard.getContinuity();
        String main = frame ? StrUtil.blankToDefault(storyboard.getImagePrompt(), storyboard.getSceneText())
                : StrUtil.blankToDefault(storyboard.getVideoPrompt(), storyboard.getSceneText());
        if (StrUtil.isNotBlank(main)) {
            parts.add(StrUtil.trim(main));
        }
        if (StrUtil.isNotBlank(storyboard.getShotType()) || StrUtil.isNotBlank(storyboard.getCameraMove())) {
            parts.add("镜头：" + StrUtil.join(" ", StrUtil.emptyIfNull(storyboard.getShotType()),
                    StrUtil.emptyIfNull(storyboard.getCameraMove())).trim());
        }
        Map<Long, AiDramaCharacterDO> characterMap = convertMap(assetService.getCharacterList(project.getId()),
                AiDramaCharacterDO::getId);
        // 场景
        AiDramaLocationDO location = getStoryboardLocation(storyboard, project);
        if (location != null) {
            parts.add("场景：" + location.getName() + appendDetail(location.getDescription()));
        }
        // 出场角色：形象与站位，只使用本项目角色的
        Map<Long, AiDramaCharacterAppearanceDO> appearanceMap = convertMap(
                assetService.getAppearanceList(getAppearanceIds(storyboard)), AiDramaCharacterAppearanceDO::getId);
        List<String> names = convertList(storyboard.getCharacters(), ref -> {
            AiDramaCharacterAppearanceDO appearance = ref.getAppearanceId() != null ? appearanceMap.get(ref.getAppearanceId()) : null;
            AiDramaCharacterDO character = appearance != null ? characterMap.get(appearance.getCharacterId()) : null;
            if (character == null || isFrameAbsent(storyboard, frame, character.getName())) {
                return null;
            }
            return character.getName() + appendDetail(StrUtil.blankToDefault(appearance.getDescription(), character.getDescription()))
                    + (StrUtil.isNotBlank(ref.getSlot()) ? "位于" + ref.getSlot() : "");
        });
        if (!names.isEmpty()) {
            parts.add("角色：" + String.join("；", names));
        }
        // 道具
        List<String> titles = convertList(getStoryboardProps(storyboard, project.getId()), AiDramaVisualAssetDO::getTitle);
        if (!titles.isEmpty()) {
            parts.add("道具：" + String.join("、", titles));
        }
        // 摄影规则
        String photography = buildPhotographyText(storyboard.getPhotographyRules());
        if (StrUtil.isNotEmpty(photography)) {
            parts.add("摄影：" + photography);
        }
        if (frame) {
            // 关键帧：分镜规划的起始状态、空间锚点、0 秒出镜角色与背景群演，只画本镜 0 秒的静态首帧
            if (continuity != null && StrUtil.isNotBlank(continuity.getStartState())) {
                parts.add("起始状态（只画此刻，不提前画出结束动作）：" + StrUtil.trim(continuity.getStartState()));
            }
            if (continuity != null && StrUtil.isNotBlank(continuity.getSpatialAnchor())) {
                parts.add("空间与视线：" + StrUtil.trim(continuity.getSpatialAnchor()));
            }
            if (continuity != null && continuity.getPresentCharacters() != null) {
                parts.add("0 秒出镜角色：" + (continuity.getPresentCharacters().isEmpty() ? "无"
                        : String.join("、", continuity.getPresentCharacters())) + "，其他角色不出现在画面中");
            }
            if (continuity != null && StrUtil.isNotBlank(continuity.getBackgroundExtras())) {
                parts.add("背景群演（匿名，不画成具名角色）：" + StrUtil.trim(continuity.getBackgroundExtras()));
            }
            // 关键帧：画面焦点、导演指令
            if (continuity != null && StrUtil.isNotBlank(continuity.getFrameFocus())) {
                parts.add("画面焦点：" + StrUtil.trim(continuity.getFrameFocus()));
            }
            if (continuity != null && StrUtil.isNotBlank(continuity.getKeyframeDirective())) {
                parts.add("构图要求：" + StrUtil.trim(continuity.getKeyframeDirective()));
            }
        } else {
            // 视频：表演（没有表演指导时，使用分镜规划的表演节拍）、台词
            List<AiDramaStoryboardDO.ActingNote> actingNotes = CollUtil.isNotEmpty(storyboard.getActingNotes())
                    ? storyboard.getActingNotes() : continuity != null ? continuity.getPerformanceBeats() : null;
            if (CollUtil.isNotEmpty(actingNotes)) {
                parts.add("表演：" + String.join("；", convertList(actingNotes,
                        note -> note.getName() + "：" + note.getActing())));
            }
            if (CollUtil.isNotEmpty(storyboard.getDialogues())) {
                List<String> lines = convertList(storyboard.getDialogues(), dialogue -> {
                    AiDramaCharacterDO speaker = dialogue.getCharacterId() != null ? characterMap.get(dialogue.getCharacterId()) : null;
                    return (speaker != null ? speaker.getName() : "旁白") + "：" + dialogue.getLine();
                });
                parts.add("台词：" + String.join("；", lines));
            }
            // 发言角色的声音设定：语音合成不支持按描述设计音色，作为文字说明交给视频模型
            List<String> voices = convertList(getSpeakerIds(storyboard, characterMap), speakerId -> {
                AiDramaCharacterDO speaker = characterMap.get(speakerId);
                return StrUtil.isNotBlank(speaker.getVoiceDescription())
                        ? speaker.getName() + "：" + StrUtil.trim(speaker.getVoiceDescription()) : null;
            });
            if (!voices.isEmpty()) {
                parts.add("声音：" + String.join("；", voices));
            }
        }
        String style = assetService.buildStyleText(project);
        if (StrUtil.isNotBlank(style)) {
            parts.add("画风：" + style);
        }
        return String.join("。", parts);
    }

    /**
     * 拼接摄影规则：光线、人物、景深、色调；其它项不写入提示词
     *
     * 例如说：光线：侧逆光 柔和；人物：林夏 画面左侧 站立 面向右；景深：浅景深；色调：冷蓝
     */
    @VisibleForTesting
    static String buildPhotographyText(Map<String, Object> rules) {
        if (CollUtil.isEmpty(rules)) {
            return null;
        }
        List<String> items = new ArrayList<>();
        PHOTOGRAPHY_LABELS.forEach((key, label) -> {
            String text = flattenText(rules.get(key));
            if (StrUtil.isNotBlank(text)) {
                items.add(label + "：" + text);
            }
        });
        return String.join("；", items);
    }

    /**
     * 把 JSON 值展开为文本：对象的值用空格连接，数组的元素用逗号连接
     */
    private static String flattenText(Object value) {
        if (value instanceof Map) {
            return CollUtil.join(convertList(((Map<?, ?>) value).values(), item -> StrUtil.emptyToNull(flattenText(item))), " ");
        }
        if (value instanceof Collection) {
            return CollUtil.join(convertList((Collection<?>) value, item -> StrUtil.emptyToNull(flattenText(item))), "，");
        }
        return value != null ? StrUtil.trim(String.valueOf(value)) : null;
    }

    /**
     * 拼接补充说明：不为空时用括号包裹
     */
    private static String appendDetail(String detail) {
        return StrUtil.isNotBlank(detail) ? "（" + StrUtil.trim(detail) + "）" : "";
    }

    // ========== 镜头视频：同步 ==========

    @Override
    public int syncStoryboardVideo() {
        List<AiDramaStoryboardDO> list = storyboardMapper.selectListByVideoStatus(Arrays.asList(
                AiDramaStatusEnum.IN_PROGRESS.getStatus(), AiDramaStatusEnum.SAVING.getStatus()), SYNC_LIMIT);
        int count = 0;
        for (AiDramaStoryboardDO storyboard : list) {
            try {
                if (fetchStoryboardVideo(storyboard)) {
                    count++;
                }
            } catch (Exception e) {
                log.warn("[syncStoryboardVideo][storyboard({}) task({}) 查询或转存失败，下轮重试]",
                        storyboard.getId(), storyboard.getTaskId(), e);
            }
        }
        return count;
    }

    @Override
    public void syncStoryboardVideo(Long id) {
        // 1. 校验生成中、转存中
        AiDramaStoryboardDO storyboard = storyboardMapper.selectById(id);
        if (storyboard == null) {
            throw exception(DRAMA_STORYBOARD_NOT_EXISTS);
        }
        if (!ObjectUtils.equalsAny(storyboard.getVideoStatus(), AiDramaStatusEnum.IN_PROGRESS.getStatus(),
                AiDramaStatusEnum.SAVING.getStatus())) {
            throw exception(DRAMA_TASK_NOT_RUNNING);
        }

        // 2. 查询服务商，成功后转存
        try {
            fetchStoryboardVideo(storyboard);
        } catch (Exception e) {
            log.warn("[syncStoryboardVideo][storyboard({}) task({}) 查询或转存失败]", id, storyboard.getTaskId(), e);
            throw exception(DRAMA_TASK_SYNC_FAIL, StrUtil.blankToDefault(e.getMessage(), "查询服务商失败"));
        }
    }

    /**
     * 查询一个镜头视频：未结束的保持原状态；成功先置为转存中，再转存视频、截取尾帧
     *
     * 回写都以“状态未变、且仍是查询的服务商任务”为条件：查询、转存期间被标记失败又重新提交时，不覆盖新一轮
     *
     * @return 是否有状态变化
     */
    private boolean fetchStoryboardVideo(AiDramaStoryboardDO storyboard) {
        // 1. 项目、模型、密钥：按生成时的模型取当前密钥；已删除则置为已失败
        AiDramaProjectDO project = projectService.getProject(storyboard.getProjectId());
        if (project == null) {
            updateVideoFail(storyboard.getId(), storyboard.getVideoStatus(), storyboard.getTaskId(), "项目已删除");
            return true;
        }
        AiModelDO model = modelService.getModel(storyboard.getVideoModelId());
        if (model == null) {
            updateVideoFail(storyboard.getId(), storyboard.getVideoStatus(), storyboard.getTaskId(), "视频模型已删除");
            return true;
        }
        AiApiKeyDO apiKey = apiKeyService.getApiKey(model.getKeyId());
        if (apiKey == null) {
            updateVideoFail(storyboard.getId(), storyboard.getVideoStatus(), storyboard.getTaskId(), "渠道密钥已删除");
            return true;
        }
        // 2. 查询服务商：首帧按本次提交记录（可灵的查询路径依赖创建时是否有首帧），不按提交后的关键帧、资产重新推导；
        // 没有提交记录的旧任务，按当前的起始帧推导（连续多镜段的起始帧作为参考图提交，没有首帧）
        AiDramaVideoSubmissionDO submission = storyboard.getTaskId() != null
                ? videoSubmissionMapper.selectByStoryboardIdAndTaskId(storyboard.getId(), storyboard.getTaskId()) : null;
        String firstFrameUrl = submission != null ? submission.getFirstFrameUrl()
                : isMultiShot(storyboard) ? null : getFirstFrameUrl(storyboard, project);
        AiVideoTaskResult result = videoService.getVideoTask(buildVideo(storyboard, model, null,
                firstFrameUrl, null), apiKey);
        if (!result.isFinished()) {
            return false;
        }
        if (!result.isSucceeded()) {
            updateVideoFail(storyboard.getId(), storyboard.getVideoStatus(), storyboard.getTaskId(),
                    StrUtil.blankToDefault(result.getErrorMessage(), "生成失败"));
            return true;
        }
        // 3.1 成功：先更新为转存中，再转存
        if (Objects.equals(storyboard.getVideoStatus(), AiDramaStatusEnum.IN_PROGRESS.getStatus())) {
            int updated = storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(),
                    AiDramaStatusEnum.IN_PROGRESS.getStatus(), storyboard.getTaskId(),
                    new AiDramaStoryboardDO().setVideoStatus(AiDramaStatusEnum.SAVING.getStatus()));
            if (updated == 0) {
                return false;
            }
        }
        AiVideoOutput output = CollUtil.findOne(result.getVideos(),
                item -> StrUtil.isNotBlank(item.getUrl()) || item.getData() != null);
        if (output == null) {
            updateVideoFail(storyboard.getId(), AiDramaStatusEnum.SAVING.getStatus(), storyboard.getTaskId(),
                    "服务商返回成功，但没有视频结果");
            return true;
        }
        byte[] content = output.getData() != null ? output.getData() : HttpUtil.downloadBytes(output.getUrl());
        String videoUrl = fileApi.createFile(content);
        // 3.2 截取尾帧，供下一镜承接；失败不影响视频结果，此时置空，清除上一个视频的尾帧
        String lastFrameUrl = StrUtil.nullToEmpty(extractLastFrameQuietly(storyboard.getId(), videoUrl));
        int updated = storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(), AiDramaStatusEnum.SAVING.getStatus(),
                storyboard.getTaskId(), new AiDramaStoryboardDO().setVideoUrl(videoUrl).setLastFrameUrl(lastFrameUrl)
                        .setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()));
        // 3.3 提交记录：记录结果
        if (updated > 0 && storyboard.getTaskId() != null) {
            videoSubmissionMapper.updateByStoryboardIdAndTaskId(storyboard.getId(), storyboard.getTaskId(),
                    new AiDramaVideoSubmissionDO().setVideoUrl(videoUrl).setLastFrameUrl(lastFrameUrl)
                            .setStatus(AiDramaStatusEnum.SUCCESS.getStatus()));
        }
        return true;
    }

    /**
     * 截取视频的最后一帧并转存；失败时返回空
     */
    private String extractLastFrameQuietly(Long id, String videoUrl) {
        try {
            byte[] content = AiFfmpegUtils.extractLastFrame(ffmpegPath, fileApi.presignGetUrlQuietly(videoUrl));
            return fileApi.createFile(content, "last-frame.png");
        } catch (Exception e) {
            log.warn("[extractLastFrameQuietly][storyboard({}) 截取尾帧失败]", id, e);
            return null;
        }
    }

    // ========== 镜头视频：生成任务 ==========

    @Override
    public PageResult<AiDramaStoryboardDO> getStoryboardTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                                 Collection<Long> projectIds) {
        return storyboardMapper.selectTaskPage(pageParam, reqVO, projectIds);
    }

    @Override
    public void updateStoryboardVideoFail(Long id, String errorMessage) {
        // 1. 校验进行中
        AiDramaStoryboardDO storyboard = storyboardMapper.selectById(id);
        if (storyboard == null) {
            throw exception(DRAMA_STORYBOARD_NOT_EXISTS);
        }
        if (!AiDramaStatusEnum.isRunning(storyboard.getVideoStatus())) {
            throw exception(DRAMA_TASK_NOT_RUNNING);
        }

        // 2. 以读取到的状态、任务编号为条件更新：期间已结束或已重新提交时，不结束新的一轮
        updateVideoFail(id, storyboard.getVideoStatus(), storyboard.getTaskId(), errorMessage);
    }

    @Override
    public int timeoutStoryboardVideo(LocalDateTime updateTime, String errorMessage) {
        int count = 0;
        // 以查询到的状态、任务编号为条件更新：期间已结束或已重新提交时，不结束新的一轮
        for (AiDramaStoryboardDO storyboard : storyboardMapper.selectListByVideoStatusAndUpdateTimeLt(Arrays.asList(
                AiDramaStatusEnum.PENDING.getStatus(), AiDramaStatusEnum.IN_PROGRESS.getStatus(),
                AiDramaStatusEnum.SAVING.getStatus()), updateTime)) {
            count += updateVideoFail(storyboard.getId(), storyboard.getVideoStatus(), storyboard.getTaskId(), errorMessage);
        }
        return count;
    }

    /**
     * 以状态、任务编号为条件，把镜头视频置为已失败；对应的提交记录一并记录失败原因
     *
     * 待处理（服务商尚未受理）时失败，提交记录的结果未知：只记录原因，仍为待处理，由用户确认任务编号或确认失败
     *
     * @return 更新的数量：状态或任务编号已变时为 0
     */
    private int updateVideoFail(Long id, Integer whereStatus, String whereTaskId, String errorMessage) {
        int updated = storyboardMapper.updateByIdAndVideoStatusAndTaskId(id, whereStatus, whereTaskId, new AiDramaStoryboardDO()
                .setVideoStatus(AiDramaStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage));
        if (updated > 0 && whereTaskId != null) {
            AiDramaVideoSubmissionDO updateObj = new AiDramaVideoSubmissionDO().setErrorMessage(errorMessage);
            if (ObjUtil.notEqual(whereStatus, AiDramaStatusEnum.PENDING.getStatus())) {
                updateObj.setStatus(AiDramaStatusEnum.FAIL.getStatus());
            }
            videoSubmissionMapper.updateByStoryboardIdAndTaskId(id, whereTaskId, updateObj);
        }
        return updated;
    }

    // ========== 镜头视频：提交记录 ==========

    @Override
    public List<AiDramaVideoSubmissionDO> getVideoSubmissionListMy(Long userId, Long storyboardId) {
        validateStoryboardMy(storyboardId, userId);
        return videoSubmissionMapper.selectListByStoryboardId(storyboardId);
    }

    @Override
    public void resolveVideoSubmissionMy(Long userId, AiDramaVideoSubmissionResolveReqVO reqVO) {
        // 1.1 校验提交结果未知
        AiDramaVideoSubmissionDO submission = validateVideoSubmissionUnknown(reqVO.getId(), userId);
        // 1.2 校验分镜：仍是镜头当前的提交（之后又提交过时，不能把旧提交确认为当前任务；历史视频请使用“选用视频”）；
        //     仍在提交本次请求（例如进程中断）时可以确认，否则镜头视频不能在生成中
        AiDramaStoryboardDO storyboard = validateSubmissionStoryboard(submission);
        if (ObjUtil.notEqual(storyboard.getTaskId(), submission.getRequestId())) {
            throw exception(DRAMA_VIDEO_SUBMISSION_NOT_UNKNOWN);
        }
        if (!isSubmitting(storyboard, submission)) {
            validateVideoNotRunning(storyboard);
        }

        // 2. 分镜改为生成中，由同步任务查询结果：以读取到的状态、任务编号为条件，期间被处理时不覆盖
        String taskId = StrUtil.trim(reqVO.getTaskId());
        int updated = storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(), storyboard.getVideoStatus(),
                storyboard.getTaskId(), new AiDramaStoryboardDO().setTaskId(taskId)
                        .setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus())
                        .setVideoModelId(submission.getModelId()).setVideoModel(submission.getModel()));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
        videoSubmissionMapper.updateByIdAndStatus(submission.getId(), AiDramaStatusEnum.PENDING.getStatus(),
                new AiDramaVideoSubmissionDO().setTaskId(taskId).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));
    }

    @Override
    public void confirmVideoSubmissionFailMy(Long userId, Long id) {
        // 1. 校验提交结果未知
        AiDramaVideoSubmissionDO submission = validateVideoSubmissionUnknown(id, userId);

        // 2.1 分镜仍在提交本次请求（例如进程中断）时，一并置为已失败
        String errorMessage = "已确认服务商未创建任务";
        AiDramaStoryboardDO storyboard = validateSubmissionStoryboard(submission);
        if (isSubmitting(storyboard, submission)) {
            updateVideoFail(storyboard.getId(), storyboard.getVideoStatus(), storyboard.getTaskId(), errorMessage);
        }
        // 2.2 提交记录置为已失败
        videoSubmissionMapper.updateByIdAndStatus(id, AiDramaStatusEnum.PENDING.getStatus(), new AiDramaVideoSubmissionDO()
                .setStatus(AiDramaStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage));
    }

    @Override
    public void useVideoSubmissionMy(Long userId, Long id, boolean previous) {
        // 1.1 校验提交记录、分镜：镜头视频不在生成中
        AiDramaVideoSubmissionDO submission = validateVideoSubmissionMy(id, userId);
        AiDramaStoryboardDO storyboard = validateSubmissionStoryboard(submission);
        validateVideoNotRunning(storyboard);
        // 1.2 要使用的视频：提交前的视频，或本次提交成功的视频
        AiDramaStoryboardDO updateObj;
        if (previous) {
            AiDramaVideoSubmissionDO.VideoSnapshot video = submission.getPreviousVideo();
            if (video == null || StrUtil.isBlank(video.getVideoUrl())) {
                throw exception(DRAMA_VIDEO_SUBMISSION_NO_VIDEO);
            }
            updateObj = new AiDramaStoryboardDO().setTaskId(video.getTaskId()).setVideoUrl(video.getVideoUrl())
                    .setLastFrameUrl(StrUtil.nullToEmpty(video.getLastFrameUrl()));
        } else {
            if (ObjUtil.notEqual(submission.getStatus(), AiDramaStatusEnum.SUCCESS.getStatus())
                    || StrUtil.isBlank(submission.getVideoUrl())) {
                throw exception(DRAMA_VIDEO_SUBMISSION_NO_VIDEO);
            }
            updateObj = new AiDramaStoryboardDO().setTaskId(submission.getTaskId()).setVideoUrl(submission.getVideoUrl())
                    .setLastFrameUrl(StrUtil.nullToEmpty(submission.getLastFrameUrl()))
                    .setVideoModelId(submission.getModelId()).setVideoModel(submission.getModel());
        }

        // 2. 作为已完成的镜头视频：以读取到的状态、任务编号为条件，期间提交了生成时不覆盖
        int updated = storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(), storyboard.getVideoStatus(),
                storyboard.getTaskId(), updateObj.setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    private AiDramaVideoSubmissionDO validateVideoSubmissionMy(Long id, Long userId) {
        AiDramaVideoSubmissionDO submission = videoSubmissionMapper.selectById(id);
        if (submission == null) {
            throw exception(DRAMA_VIDEO_SUBMISSION_NOT_EXISTS);
        }
        projectService.validateProjectMy(submission.getProjectId(), userId);
        return submission;
    }

    /**
     * 校验提交记录存在，且提交结果未知：待处理，且没有上游任务编号
     */
    private AiDramaVideoSubmissionDO validateVideoSubmissionUnknown(Long id, Long userId) {
        AiDramaVideoSubmissionDO submission = validateVideoSubmissionMy(id, userId);
        if (ObjUtil.notEqual(submission.getStatus(), AiDramaStatusEnum.PENDING.getStatus()) || submission.getTaskId() != null) {
            throw exception(DRAMA_VIDEO_SUBMISSION_NOT_UNKNOWN);
        }
        return submission;
    }

    private AiDramaStoryboardDO validateSubmissionStoryboard(AiDramaVideoSubmissionDO submission) {
        AiDramaStoryboardDO storyboard = storyboardMapper.selectById(submission.getStoryboardId());
        if (storyboard == null) {
            throw exception(DRAMA_STORYBOARD_NOT_EXISTS);
        }
        return storyboard;
    }

    /**
     * 分镜是否仍在提交该请求：待处理，且任务编号为该请求编号
     */
    private static boolean isSubmitting(AiDramaStoryboardDO storyboard, AiDramaVideoSubmissionDO submission) {
        return Objects.equals(storyboard.getVideoStatus(), AiDramaStatusEnum.PENDING.getStatus())
                && Objects.equals(storyboard.getTaskId(), submission.getRequestId());
    }

    // ========== 关键帧 ==========

    @Override
    public void generateStoryboardFrameMy(Long userId, AiDramaStoryboardFrameReqVO reqVO) {
        // 1. 校验分镜：镜头视频不在生成中（查询路径依赖起始帧），剧本未修改
        AiDramaStoryboardDO storyboard = validateStoryboardMy(reqVO.getId(), userId);
        validateVideoNotRunning(storyboard);
        AiDramaProjectDO project = projectService.validateProjectExists(storyboard.getProjectId());
        validateScriptNotChanged(project);

        // 2. 生成
        generateStoryboardFrame(storyboard, project, reqVO.getModelId(), reqVO.getRevisePrompt());
    }

    @Override
    public int generateStoryboardFrameBatchMy(Long userId, AiDramaStoryboardFrameBatchReqVO reqVO) {
        // 1.1 校验项目：剧本未修改
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        validateScriptNotChanged(project);
        // 1.2 当前剧本中，镜头序号在范围内的镜头
        AiDramaScriptDO script = scriptService.getScriptByProjectId(project.getId());
        Long scriptId = script != null ? script.getId() : null;
        List<AiDramaStoryboardDO> storyboards = filterList(storyboardMapper.selectListByProjectId(project.getId()), item ->
                Objects.equals(item.getScriptId(), scriptId) && item.getSceneNo() != null
                        && item.getSceneNo() >= reqVO.getStartSceneNo() && item.getSceneNo() <= reqVO.getEndSceneNo());
        if (storyboards.isEmpty()) {
            throw exception(DRAMA_STORYBOARD_EMPTY);
        }
        if (storyboards.size() > FRAME_BATCH_MAX) {
            throw invalidParamException("一次最多生成 {} 个镜头的关键帧", FRAME_BATCH_MAX);
        }

        // 2. 逐个提交：已有可用的关键帧、关键帧出图中、镜头视频生成中的跳过；输入已修改的关键帧重新生成
        int count = 0;
        for (AiDramaStoryboardDO storyboard : storyboards) {
            AiDramaVisualAssetDO frame = assetService.getStoryboardFrame(storyboard.getId());
            if (AiDramaStatusEnum.isRunning(storyboard.getVideoStatus()) || (frame != null
                    && (AiDramaStatusEnum.isRunning(frame.getStatus()) || isCurrentFrame(frame, storyboard, project)))) {
                continue;
            }
            generateStoryboardFrame(storyboard, project, reqVO.getModelId(), null);
            count++;
        }
        return count;
    }

    /**
     * 生成镜头关键帧：有修正说明时，以当前关键帧为参考修正构图
     */
    private void generateStoryboardFrame(AiDramaStoryboardDO storyboard, AiDramaProjectDO project, Long modelId,
                                         String revisePrompt) {
        // 1. 修正构图时需要已有关键帧
        AiDramaVisualAssetDO frame = assetService.getStoryboardFrame(storyboard.getId());
        String previousImageUrl = frame != null ? StrUtil.emptyToNull(frame.getImageUrl()) : null;
        boolean revise = StrUtil.isNotBlank(revisePrompt);
        if (revise && previousImageUrl == null) {
            throw exception(DRAMA_FIRST_FRAME_EMPTY);
        }

        // 2.1 原始输入：提示词、参考图、本镜生效的道具；输入摘要按原始输入计算，修正构图后仍对应当前的输入
        String basePrompt = buildFramePrompt(storyboard, project);
        AiDramaVisualAssetDO.ReferenceImages baseReferences = buildFrameReferenceImages(storyboard, project);
        String sourceHash = buildFrameSourceHash(basePrompt, baseReferences);
        // 2.2 修正构图时，当前关键帧作为第 1 张参考图，并在提示词前带上修正说明
        List<String> referenceUrls = new ArrayList<>();
        if (revise) {
            referenceUrls.add(previousImageUrl);
        }
        baseReferences.getUrls().forEach(url -> {
            if (!referenceUrls.contains(url)) {
                referenceUrls.add(url);
            }
        });
        String prompt = revise ? "以参考图1（当前画面）为基础修正构图：" + StrUtil.trim(revisePrompt)
                + "。保持人物、场景与画风一致。" + basePrompt : basePrompt;

        // 3. 提交出图
        assetService.generateStoryboardFrame(storyboard, modelId, prompt, new AiDramaVisualAssetDO.ReferenceImages()
                        .setUrls(referenceUrls).setPropsVersion(baseReferences.getPropsVersion())
                        .setPreviousImageUrl(revise ? previousImageUrl : null),
                sourceHash, project.getComposeAspectRatio());
    }

    /**
     * 构建关键帧的提示词：画幅、镜头的图片提示词
     */
    private String buildFramePrompt(AiDramaStoryboardDO storyboard, AiDramaProjectDO project) {
        return "短剧镜头的关键帧画面，画幅 " + StrUtil.blankToDefault(project.getComposeAspectRatio(), "9:16")
                + "。" + buildStoryboardPrompt(storyboard, project, true);
    }

    /**
     * 构建关键帧的参考图：出场角色形象、场景、道具的选用图，纯文字重画时不带；道具版本为本镜生效的道具
     */
    private AiDramaVisualAssetDO.ReferenceImages buildFrameReferenceImages(AiDramaStoryboardDO storyboard,
                                                                         AiDramaProjectDO project) {
        List<String> referenceUrls = new ArrayList<>();
        AiDramaStoryboardDO.Continuity continuity = storyboard.getContinuity();
        if (continuity == null || ObjUtil.notEqual(continuity.getKeyframeReferenceMode(), KEYFRAME_REFERENCE_MODE_TEXT_ONLY)) {
            Map<Long, AiDramaCharacterDO> characterMap = convertMap(assetService.getCharacterList(project.getId()),
                    AiDramaCharacterDO::getId);
            referenceUrls.addAll(getReferenceImages(storyboard, project, characterMap, true).keySet());
        }
        String propsVersion = StrUtil.join(",", convertList(getStoryboardProps(storyboard, project.getId()),
                AiDramaVisualAssetDO::getId));
        return new AiDramaVisualAssetDO.ReferenceImages().setUrls(referenceUrls).setPropsVersion(propsVersion);
    }

    /**
     * 计算关键帧的输入摘要：提示词、参考图、道具版本
     */
    private static String buildFrameSourceHash(String prompt, AiDramaVisualAssetDO.ReferenceImages referenceImages) {
        return DigestUtil.sha256Hex(prompt + referenceImages.getUrls() + referenceImages.getPropsVersion());
    }

    /**
     * 判断关键帧是否可用：出图成功，且输入摘要对应镜头当前的提示词、参考图、道具
     */
    private boolean isCurrentFrame(AiDramaVisualAssetDO frame, AiDramaStoryboardDO storyboard, AiDramaProjectDO project) {
        if (frame == null || ObjUtil.notEqual(frame.getStatus(), AiDramaStatusEnum.SUCCESS.getStatus())
                || StrUtil.isBlank(frame.getImageUrl())) {
            return false;
        }
        return Objects.equals(frame.getSourceHash(), buildFrameSourceHash(buildFramePrompt(storyboard, project),
                buildFrameReferenceImages(storyboard, project)));
    }

    @Override
    public void usePreviousLastFrameMy(Long userId, Long id) {
        // 1.1 校验分镜：镜头视频不在生成中
        AiDramaStoryboardDO storyboard = validateStoryboardMy(id, userId);
        validateVideoNotRunning(storyboard);
        // 1.2 上一个镜头：同一剧本版本，按序号排序；视频需已完成
        List<AiDramaStoryboardDO> storyboards = filterList(storyboardMapper.selectListByProjectId(storyboard.getProjectId()),
                item -> Objects.equals(item.getScriptId(), storyboard.getScriptId()));
        int index = CollUtil.indexOf(storyboards, item -> Objects.equals(item.getId(), id));
        AiDramaStoryboardDO previous = index > 0 ? storyboards.get(index - 1) : null;
        if (previous == null || ObjUtil.notEqual(previous.getVideoStatus(), AiDramaStatusEnum.SUCCESS.getStatus())
                || StrUtil.isBlank(previous.getVideoUrl())) {
            throw exception(DRAMA_PREVIOUS_STORYBOARD_NOT_EXISTS);
        }

        // 2. 上一镜的尾帧：生成时已截取的直接使用，否则现在截取并转存
        String lastFrameUrl = previous.getLastFrameUrl();
        if (StrUtil.isBlank(lastFrameUrl)) {
            try {
                byte[] content = AiFfmpegUtils.extractLastFrame(ffmpegPath, fileApi.presignGetUrlQuietly(previous.getVideoUrl()));
                lastFrameUrl = fileApi.createFile(content, "last-frame.png");
            } catch (Exception e) {
                log.error("[usePreviousLastFrameMy][storyboard({}) previous({}) 截取尾帧失败]", id, previous.getId(), e);
                throw exception(DRAMA_FFMPEG_FAIL, "截取上一镜头的尾帧", StrUtil.blankToDefault(e.getMessage(), "FFmpeg 执行失败"));
            }
            storyboardMapper.updateById(new AiDramaStoryboardDO().setId(previous.getId()).setLastFrameUrl(lastFrameUrl));
        }

        // 3. 作为本镜的手动起始帧，并改为使用起始帧
        AiDramaStoryboardDO.Continuity continuity = ObjUtil.defaultIfNull(storyboard.getContinuity(),
                AiDramaStoryboardDO.Continuity::new);
        continuity.setManualStartFrame(new AiDramaStoryboardDO.ManualStartFrame().setUrl(lastFrameUrl)).setUseFirstFrame(true);
        storyboardMapper.updateById(new AiDramaStoryboardDO().setId(id).setContinuity(continuity));
    }

    // ========== 素材 ==========

    @Override
    public void renderStoryboardMaterialMy(Long userId, Long id) {
        // 1. 校验分镜：镜头视频不在生成中，且有素材图片
        AiDramaStoryboardDO storyboard = validateStoryboardMy(id, userId);
        validateVideoNotRunning(storyboard);
        List<String> materialUrls = storyboard.getContinuity() != null ? filterList(
                CollUtil.emptyIfNull(storyboard.getContinuity().getMaterialUrls()), StrUtil::isNotBlank) : Collections.emptyList();
        if (materialUrls.isEmpty()) {
            throw exception(DRAMA_MATERIAL_EMPTY);
        }
        AiDramaProjectDO project = projectService.validateProjectExists(storyboard.getProjectId());

        // 2. 按项目画幅、镜头时长渲染，转存
        String videoUrl;
        try {
            byte[] content = AiFfmpegUtils.renderImages(ffmpegPath, convertList(materialUrls, fileApi::presignGetUrlQuietly),
                    project.getComposeAspectRatio(), ObjUtil.defaultIfNull(storyboard.getDuration(), DEFAULT_DURATION));
            videoUrl = fileApi.createFile(content, "material.mp4");
        } catch (Exception e) {
            log.error("[renderStoryboardMaterialMy][storyboard({}) 渲染素材失败]", id, e);
            throw exception(DRAMA_FFMPEG_FAIL, "渲染素材", StrUtil.blankToDefault(e.getMessage(), "FFmpeg 执行失败"));
        }

        // 3. 直接作为已完成的镜头视频，尾帧为最后一张素材：以读取到的状态、任务编号为条件，期间提交了生成时不覆盖；
        //    任务编号置为空串，与导入视频相同
        int updated = storyboardMapper.updateByIdAndVideoStatusAndTaskId(id, storyboard.getVideoStatus(), storyboard.getTaskId(),
                new AiDramaStoryboardDO().setTaskId("").setVideoUrl(videoUrl).setLastFrameUrl(CollUtil.getLast(materialUrls))
                        .setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    @Override
    public void importStoryboardVideoMy(Long userId, AiDramaStoryboardImportVideoReqVO reqVO) {
        // 1.1 校验分镜：镜头视频不在生成中
        AiDramaStoryboardDO storyboard = validateStoryboardMy(reqVO.getId(), userId);
        validateVideoNotRunning(storyboard);
        // 1.2 校验视频：能读取到画面，边长不超过上限
        AiFfmpegUtils.VideoInfo info;
        try {
            info = AiFfmpegUtils.probeVideo(ffprobePath, fileApi.presignGetUrlQuietly(reqVO.getFileUrl()));
        } catch (Exception e) {
            log.warn("[importStoryboardVideoMy][storyboard({}) url({}) 读取视频失败]", reqVO.getId(), reqVO.getFileUrl(), e);
            throw exception(DRAMA_FFMPEG_FAIL, "读取视频", StrUtil.blankToDefault(e.getMessage(), "FFprobe 执行失败"));
        }
        if (info.getDuration() <= 0 || info.getWidth() > IMPORT_VIDEO_MAX_SIDE || info.getHeight() > IMPORT_VIDEO_MAX_SIDE) {
            throw invalidParamException("视频的宽、高不能超过 {}，且时长需大于 0", IMPORT_VIDEO_MAX_SIDE);
        }

        // 2. 截取尾帧，直接作为已完成的镜头视频：以读取到的状态、任务编号为条件，期间提交了生成时不覆盖；
        //    任务编号置为空串，之前结果未知的提交不再是镜头当前的任务，不能再填写任务编号接管镜头
        String lastFrameUrl = extractLastFrameQuietly(storyboard.getId(), reqVO.getFileUrl());
        int updated = storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(), storyboard.getVideoStatus(),
                storyboard.getTaskId(), new AiDramaStoryboardDO().setTaskId("").setVideoUrl(reqVO.getFileUrl())
                        .setLastFrameUrl(StrUtil.nullToEmpty(lastFrameUrl)).setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    // ========== 声音 ==========

    @Override
    public AiDramaStoryboardVoiceRespVO getStoryboardVoiceMy(Long userId, Long id) {
        // 1. 校验分镜
        AiDramaStoryboardDO storyboard = validateStoryboardMy(id, userId);
        AiDramaProjectDO project = projectService.validateProjectExists(storyboard.getProjectId());

        // 2.1 发言角色：与生成镜头视频时的识别一致
        Map<Long, AiDramaCharacterDO> characterMap = convertMap(assetService.getCharacterList(project.getId()),
                AiDramaCharacterDO::getId);
        Map<Long, AiDramaAudioDO> audioMap = convertMap(assetService.getAudioList(project.getId()), AiDramaAudioDO::getId);
        List<AiDramaStoryboardVoiceRespVO.Speaker> speakers = convertList(getSpeakerIds(storyboard, characterMap), speakerId -> {
            AiDramaCharacterDO character = characterMap.get(speakerId);
            AiDramaAudioDO audio = character.getSampleAudioId() != null ? audioMap.get(character.getSampleAudioId()) : null;
            return new AiDramaStoryboardVoiceRespVO.Speaker().setCharacterId(speakerId).setName(character.getName())
                    .setVoiceDescription(character.getVoiceDescription()).setSampleAudioUrl(audio != null ? audio.getFileUrl() : null);
        });
        // 2.2 参考音频：按提交时的编号
        List<AiDramaStoryboardVoiceRespVO.ReferenceAudio> referenceAudios = new ArrayList<>();
        getReferenceAudios(storyboard, project, characterMap).forEach((url, names) -> referenceAudios.add(
                new AiDramaStoryboardVoiceRespVO.ReferenceAudio().setUrl(url)
                        .setDescription("参考音频" + (referenceAudios.size() + 1) + "为" + String.join("、", names))));
        List<Long> voiceSpeakerIds = storyboard.getContinuity() != null ? storyboard.getContinuity().getVoiceSpeakerIds() : null;
        return new AiDramaStoryboardVoiceRespVO().setExplicit(CollUtil.isNotEmpty(voiceSpeakerIds))
                .setSpeakers(speakers).setReferenceAudios(referenceAudios);
    }

    @Override
    public void updateStoryboardSpeakersMy(Long userId, AiDramaStoryboardSpeakerReqVO reqVO) {
        // 1.1 校验分镜：镜头视频不在生成中
        AiDramaStoryboardDO storyboard = validateStoryboardMy(reqVO.getId(), userId);
        validateVideoNotRunning(storyboard);
        // 1.2 校验发言角色：本项目的角色
        List<Long> characterIds = CollUtil.distinct(CollUtil.emptyIfNull(reqVO.getCharacterIds()));
        if (!convertSet(assetService.getCharacterList(storyboard.getProjectId()), AiDramaCharacterDO::getId)
                .containsAll(characterIds)) {
            throw exception(DRAMA_CHARACTER_NOT_EXISTS);
        }

        // 2. 更新：为空时按台词自动识别
        AiDramaStoryboardDO.Continuity continuity = ObjUtil.defaultIfNull(storyboard.getContinuity(),
                AiDramaStoryboardDO.Continuity::new);
        continuity.setVoiceSpeakerIds(characterIds.isEmpty() ? null : characterIds);
        storyboardMapper.updateById(new AiDramaStoryboardDO().setId(storyboard.getId()).setContinuity(continuity));
    }

}
