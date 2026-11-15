package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardVideoReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaStoryboardMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
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
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getMaxValue;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧分镜 Service 实现类
 *
 * 镜头视频复用 AI 视频的客户端（{@link cn.iocoder.yudao.module.ai.service.video.AiVideoService}），
 * 任务编号、状态、结果都存在分镜上，不写 ai_video
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

    @Resource
    private AiDramaStoryboardMapper storyboardMapper;

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
    public Long createStoryboardMy(Long userId, AiDramaStoryboardSaveReqVO createReqVO) {
        // 1. 校验项目
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);

        // 2.1 查询：项目当前的剧本；未指定场次时，排在最后
        // DONE @AI：先查询出来，后 convert；按照这个习惯；（先查剧本、末尾场次，再 toBean 后一次性 set）
        AiDramaScriptDO script = scriptService.getScriptByProjectId(createReqVO.getProjectId());
        Integer sceneNo = createReqVO.getSceneNo();
        if (sceneNo == null) {
            List<AiDramaStoryboardDO> storyboards = storyboardMapper.selectListByProjectId(createReqVO.getProjectId());
            sceneNo = ObjUtil.defaultIfNull(getMaxValue(storyboards, item -> ObjUtil.defaultIfNull(item.getSceneNo(), 0)), 0) + 1;
        }
        // 2.2 插入
        AiDramaStoryboardDO storyboard = BeanUtils.toBean(createReqVO, AiDramaStoryboardDO.class)
                .setId(null).setScriptId(script != null ? script.getId() : null).setSceneNo(sceneNo);
        storyboardMapper.insert(storyboard);
        return storyboard.getId();
    }

    @Override
    public void updateStoryboardMy(Long userId, AiDramaStoryboardSaveReqVO updateReqVO) {
        // 1. 校验存在，且镜头视频不在生成中
        AiDramaStoryboardDO storyboard = validateStoryboardMy(updateReqVO.getId(), userId);
        validateVideoNotRunning(storyboard);

        // 2. 更新：不修改所属项目、剧本；清空的场景一并清空
        AiDramaStoryboardDO updateObj = BeanUtils.toBean(updateReqVO, AiDramaStoryboardDO.class,
                o -> o.setProjectId(null).setScriptId(null));
        storyboardMapper.updateForSave(updateObj);
    }

    @Override
    public void deleteStoryboardMy(Long userId, Long id) {
        // 校验存在
        validateStoryboardMy(id, userId);
        // 删除
        storyboardMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceStoryboardList(Long projectId, List<AiDramaStoryboardDO> storyboards) {
        storyboardMapper.deleteByProjectId(projectId);
        storyboardMapper.insertBatch(storyboards);
    }

    @Override
    public void deleteStoryboardListByProjectId(Long projectId) {
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
     * 校验镜头视频不在生成中：待处理、生成中、转存中
     */
    private static void validateVideoNotRunning(AiDramaStoryboardDO storyboard) {
        if (AiDramaStatusEnum.isRunning(storyboard.getVideoStatus())) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    // ========== 镜头视频：提交 ==========

    @Override
    public void generateStoryboardVideoMy(Long userId, AiDramaStoryboardVideoReqVO reqVO) {
        // 1. 校验分镜、视频模型与其当前密钥
        AiDramaStoryboardDO storyboard = validateStoryboardMy(reqVO.getId(), userId);
        AiModelDO model = videoService.validateVideoModel(reqVO.getModelId());
        AiApiKeyDO apiKey = videoService.validateVideoApiKey(model);

        // 2. 提交
        submitStoryboardVideo(storyboard, model, apiKey);
    }

    @Override
    public int generateStoryboardVideoBatchMy(Long userId, AiDramaStoryboardVideoReqVO reqVO) {
        // 1. 校验项目、视频模型与其当前密钥
        projectService.validateProjectMy(reqVO.getId(), userId);
        AiModelDO model = videoService.validateVideoModel(reqVO.getModelId());
        AiApiKeyDO apiKey = videoService.validateVideoApiKey(model);

        // 2. 逐个提交：只处理未生成、已失败的
        int count = 0;
        for (AiDramaStoryboardDO storyboard : storyboardMapper.selectListByProjectId(reqVO.getId())) {
            if (storyboard.getVideoStatus() != null
                    && ObjUtil.notEqual(storyboard.getVideoStatus(), AiDramaStatusEnum.FAIL.getStatus())) {
                continue;
            }
            try {
                submitStoryboardVideo(storyboard, model, apiKey);
            } catch (ServiceException e) {
                // 参数不通过（例如时长不被模型支持）：记为已失败，继续下一个
                storyboardMapper.updateById(new AiDramaStoryboardDO().setId(storyboard.getId())
                        .setVideoModelId(model.getId()).setVideoStatus(AiDramaStatusEnum.FAIL.getStatus())
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

        // 2. 提交
        submitStoryboardVideo(storyboard, model, apiKey);
    }

    /**
     * 提交镜头视频：本地校验通过后置为待处理，再提交服务商；提交失败记为已失败
     *
     * 待处理时任务编号先记为本次的提交编号，提交后的回写以它为条件：提交期间被标记失败又重新提交时，不覆盖新一轮
     */
    private void submitStoryboardVideo(AiDramaStoryboardDO storyboard, AiModelDO model, AiApiKeyDO apiKey) {
        // 1. 构建并在本地校验请求：不通过直接报错，不修改记录
        validateVideoNotRunning(storyboard);
        AiDramaProjectDO project = projectService.validateProjectExists(storyboard.getProjectId());
        AiVideoDO video = buildVideo(storyboard, model, buildVideoPrompt(storyboard, project), project.getAspectRatio());
        Supplier<AiVideoTaskRef> submitter = videoService.buildVideoSubmitter(video, apiKey);

        // 2. 置为待处理，提交服务商
        String submitId = IdUtil.fastSimpleUUID();
        Integer whereStatus = AiDramaStatusEnum.PENDING.getStatus();
        storyboardMapper.updateById(new AiDramaStoryboardDO().setId(storyboard.getId())
                .setVideoModelId(model.getId()).setTaskId(submitId).setVideoStatus(whereStatus));
        try {
            AiVideoTaskRef ref = submitter.get();
            storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(), whereStatus, submitId,
                    new AiDramaStoryboardDO().setTaskId(ref.getTaskId()).setVideoStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));
        } catch (Exception e) {
            log.error("[submitStoryboardVideo][storyboard({}) 提交服务商失败]", storyboard.getId(), e);
            updateVideoFail(storyboard.getId(), whereStatus, submitId, AiVideoException.buildSubmitErrorMessage(e));
        }
    }

    /**
     * 构建视频记录（不入库）：用于复用 AI 视频的客户端；有起始帧时为图生视频，否则为文生视频
     *
     * 提交、查询使用同一份构建逻辑：部分平台（例如可灵）的查询路径依赖生成方式
     */
    @VisibleForTesting
    static AiVideoDO buildVideo(AiDramaStoryboardDO storyboard, AiModelDO model, String prompt, String aspectRatio) {
        boolean image = StrUtil.isNotBlank(storyboard.getFirstFrameUrl());
        return new AiVideoDO().setType(AiVideoTypeEnum.GENERATE.getType())
                .setMode(image ? AiVideoModeEnum.IMAGE.getMode() : AiVideoModeEnum.TEXT.getMode())
                .setPlatform(model.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                .setPrompt(prompt).setTaskId(storyboard.getTaskId())
                .setInputUrls(image ? Map.of(AiVideoInputRoleEnum.FIRST_FRAME.getRole(),
                        List.of(storyboard.getFirstFrameUrl())) : null)
                .setRatio(aspectRatio).setDuration(storyboard.getDuration());
    }

    /**
     * 拼接视频提示词：画面描述、景别运镜、场景、角色形象、道具、台词、画风；只使用本项目的资产
     */
    private String buildVideoPrompt(AiDramaStoryboardDO storyboard, AiDramaProjectDO project) {
        List<String> parts = new ArrayList<>();
        if (StrUtil.isNotBlank(storyboard.getVideoPrompt())) {
            parts.add(StrUtil.trim(storyboard.getVideoPrompt()));
        }
        if (StrUtil.isNotBlank(storyboard.getShotType()) || StrUtil.isNotBlank(storyboard.getCameraMove())) {
            parts.add("镜头：" + StrUtil.join(" ", StrUtil.emptyIfNull(storyboard.getShotType()),
                    StrUtil.emptyIfNull(storyboard.getCameraMove())).trim());
        }
        Map<Long, AiDramaCharacterDO> characterMap = convertMap(assetService.getCharacterList(project.getId()),
                AiDramaCharacterDO::getId);
        // 场景
        if (storyboard.getLocationId() != null) {
            AiDramaLocationDO location = assetService.getLocation(storyboard.getLocationId());
            if (location != null && Objects.equals(location.getProjectId(), project.getId())) {
                parts.add("场景：" + location.getName() + appendDetail(location.getDescription()));
            }
        }
        // 角色形象：只使用本项目角色的
        List<String> names = convertList(assetService.getAppearanceList(storyboard.getAppearanceIds()), appearance -> {
            AiDramaCharacterDO character = characterMap.get(appearance.getCharacterId());
            return character != null ? character.getName() + appendDetail(StrUtil.blankToDefault(
                    appearance.getDescription(), character.getDescription())) : null;
        });
        if (!names.isEmpty()) {
            parts.add("角色：" + String.join("；", names));
        }
        // 道具
        List<String> titles = convertList(assetService.getVisualAssetList(storyboard.getVisualAssetIds()),
                AiDramaVisualAssetDO::getTitle, visualAsset -> Objects.equals(visualAsset.getProjectId(), project.getId()));
        if (!titles.isEmpty()) {
            parts.add("道具：" + String.join("、", titles));
        }
        // 台词
        if (CollUtil.isNotEmpty(storyboard.getDialogues())) {
            List<String> lines = convertList(storyboard.getDialogues(), dialogue -> {
                AiDramaCharacterDO speaker = dialogue.getCharacterId() != null ? characterMap.get(dialogue.getCharacterId()) : null;
                return (speaker != null ? speaker.getName() : "旁白") + "：" + dialogue.getLine();
            });
            parts.add("台词：" + String.join("；", lines));
        }
        if (StrUtil.isNotBlank(project.getStyle())) {
            parts.add("画风：" + StrUtil.trim(project.getStyle()));
        }
        return String.join("。", parts);
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
     * 查询一个镜头视频：未结束的保持原状态；成功先置为转存中，再转存
     *
     * 回写都以“状态未变、且仍是查询的服务商任务”为条件：查询、转存期间被标记失败又重新提交时，不覆盖新一轮
     *
     * @return 是否有状态变化
     */
    private boolean fetchStoryboardVideo(AiDramaStoryboardDO storyboard) {
        // 1. 模型、密钥：按生成时的模型取当前密钥；已删除则置为已失败
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
        // 2. 查询服务商
        AiVideoTaskResult result = videoService.getVideoTask(buildVideo(storyboard, model, null, null), apiKey);
        if (!result.isFinished()) {
            return false;
        }
        if (!result.isSucceeded()) {
            updateVideoFail(storyboard.getId(), storyboard.getVideoStatus(), storyboard.getTaskId(),
                    StrUtil.blankToDefault(result.getErrorMessage(), "生成失败"));
            return true;
        }
        // 3. 成功：先更新为转存中，再转存
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
        storyboardMapper.updateByIdAndVideoStatusAndTaskId(storyboard.getId(), AiDramaStatusEnum.SAVING.getStatus(),
                storyboard.getTaskId(),
                new AiDramaStoryboardDO().setVideoUrl(videoUrl).setVideoStatus(AiDramaStatusEnum.SUCCESS.getStatus()));
        return true;
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
     * 以状态、任务编号为条件，把镜头视频置为已失败
     *
     * @return 更新的数量：状态或任务编号已变时为 0
     */
    private int updateVideoFail(Long id, Integer whereStatus, String whereTaskId, String errorMessage) {
        return storyboardMapper.updateByIdAndVideoStatusAndTaskId(id, whereStatus, whereTaskId, new AiDramaStoryboardDO()
                .setVideoStatus(AiDramaStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage));
    }

}
