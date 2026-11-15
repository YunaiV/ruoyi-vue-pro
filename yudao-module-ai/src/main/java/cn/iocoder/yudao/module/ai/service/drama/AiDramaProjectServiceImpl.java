package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaAudioDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaProjectDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaProjectMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAspectRatioEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaProjectStepEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTransitionTypeEnum;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils.ComposeRequest;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils.ComposeResult;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧项目 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiDramaProjectServiceImpl implements AiDramaProjectService {

    // DONE @AI：如果有枚举，可以简化下；（新增 AiDramaAspectRatioEnum，默认画幅直接用 RATIO_9_16，合成请求的画幅也用它 @InEnum 校验）
    /**
     * 默认转场时长（秒）
     */
    private static final Double DEFAULT_TRANSITION_DURATION = 0.5;

    @Value("${yudao.ai.drama.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;
    @Value("${yudao.ai.drama.ffprobe-path:ffprobe}")
    private String ffprobePath;
    /**
     * 水印使用的字体文件；为空时由 FFmpeg 按 fontconfig 选择，可能不支持中文
     */
    @Value("${yudao.ai.drama.font-file:}")
    private String fontFile;

    @Resource
    private AiDramaProjectMapper projectMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiDramaScriptService scriptService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiDramaAssetService assetService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiDramaStoryboardService storyboardService;

    @Resource
    private FileApi fileApi;

    // ========== 我的项目 ==========

    @Override
    public Long createProject(Long userId, AiDramaProjectSaveReqVO createReqVO) {
        // 插入：画幅为空时使用默认，从想法步骤开始，默认不公开
        AiDramaProjectDO project = BeanUtils.toBean(createReqVO, AiDramaProjectDO.class, o -> o.setId(null).setUserId(userId)
                .setAspectRatio(StrUtil.blankToDefault(createReqVO.getAspectRatio(), AiDramaAspectRatioEnum.RATIO_9_16.getRatio()))
                .setComposeTransitionType(AiDramaTransitionTypeEnum.NONE.getType())
                .setComposeTransitionDurationSeconds(DEFAULT_TRANSITION_DURATION)
                .setStatus(AiDramaProjectStepEnum.IDEA.getStep()).setPublicStatus(false));
        projectMapper.insert(project);
        return project.getId();
    }

    @Override
    public void updateProjectMy(Long userId, AiDramaProjectSaveReqVO updateReqVO) {
        // 1. 校验存在，且属于该用户
        validateProjectMy(updateReqVO.getId(), userId);

        // 2. 更新：画幅为空时使用默认
        projectMapper.updateById(BeanUtils.toBean(updateReqVO, AiDramaProjectDO.class,
                o -> o.setAspectRatio(StrUtil.blankToDefault(updateReqVO.getAspectRatio(), AiDramaAspectRatioEnum.RATIO_9_16.getRatio()))));
    }

    @Override
    public void updateProjectStatusMy(Long userId, AiDramaProjectUpdateStatusReqVO updateReqVO) {
        // 1. 校验存在，且属于该用户
        validateProjectMy(updateReqVO.getId(), userId);

        // 2. 更新当前步骤
        projectMapper.updateById(new AiDramaProjectDO().setId(updateReqVO.getId()).setStatus(updateReqVO.getStatus()));
    }

    @Override
    public void deleteProjectMy(Long id, Long userId) {
        // 1. 校验存在，且属于该用户
        validateProjectMy(id, userId);

        // 2. 删除：通过代理调用，使事务生效
        getSelf().deleteProject(id);
    }

    @Override
    public PageResult<AiDramaProjectDO> getProjectMyPage(Long userId, AiDramaProjectMyPageReqVO pageReqVO) {
        return projectMapper.selectPageByUserId(userId, pageReqVO);
    }

    @Override
    public AiDramaProjectDO validateProjectMy(Long id, Long userId) {
        AiDramaProjectDO project = projectMapper.selectById(id);
        if (project == null || ObjUtil.notEqual(project.getUserId(), userId)) {
            throw exception(DRAMA_PROJECT_NOT_EXISTS);
        }
        return project;
    }

    @Override
    public void composeProjectMy(Long userId, AiDramaComposeReqVO reqVO) {
        // 1.1 校验项目：合成中的不能重复提交
        AiDramaProjectDO project = validateProjectMy(reqVO.getProjectId(), userId);
        validateComposeNotRunning(project);
        // 1.2 校验镜头：属于该项目且视频已完成，按提交的顺序合成
        Map<Long, AiDramaStoryboardDO> storyboardMap = convertMap(
                storyboardService.getStoryboardList(project.getId()), AiDramaStoryboardDO::getId);
        List<AiDramaStoryboardDO> storyboards = convertList(reqVO.getStoryboardIds(), storyboardId -> {
            AiDramaStoryboardDO storyboard = storyboardMap.get(storyboardId);
            if (storyboard == null) {
                throw exception(DRAMA_STORYBOARD_NOT_EXISTS);
            }
            validateStoryboardSuccess(storyboard);
            return storyboard;
        });
        // 1.3 校验旁白：属于该项目
        String narrationUrl = null;
        if (reqVO.getNarrationAudioId() != null) {
            AiDramaAudioDO narration = assetService.getAudio(reqVO.getNarrationAudioId());
            if (narration == null || ObjUtil.notEqual(narration.getProjectId(), project.getId())
                    || ObjUtil.notEqual(narration.getType(), AiDramaAudioTypeEnum.NARRATION.getType())) {
                throw exception(DRAMA_AUDIO_NOT_EXISTS);
            }
            narrationUrl = fileApi.presignGetUrlQuietly(narration.getFileUrl());
        }

        // 2. 记录本次的转场设置，置为合成中；镜头选择、旁白、水印不保存
        startCompose(project, new ComposeRequest()
                .setVideoUrls(convertList(storyboards, storyboard -> fileApi.presignGetUrlQuietly(storyboard.getVideoUrl())))
                .setAspectRatio(reqVO.getAspectRatio())
                .setTransitionType(AiDramaTransitionTypeEnum.of(reqVO.getTransitionType()))
                .setTransitionDuration(ObjUtil.defaultIfNull(reqVO.getTransitionDuration(), DEFAULT_TRANSITION_DURATION))
                .setNarrationUrl(narrationUrl).setWatermarkText(StrUtil.trim(reqVO.getWatermarkText())));
    }

    /**
     * 开始合成：记录转场设置，置为合成中，再异步执行合成
     *
     * @param project 项目
     * @param request 合成参数
     */
    private void startCompose(AiDramaProjectDO project, ComposeRequest request) {
        // 1. 置为合成中：生成本次的合成任务编号，异步合成的最终回写以它为条件
        String taskId = IdUtil.fastSimpleUUID();
        projectMapper.updateById(new AiDramaProjectDO().setId(project.getId())
                .setComposeTransitionType(request.getTransitionType().getType())
                .setComposeTransitionDurationSeconds(request.getTransitionDuration())
                .setComposeStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()).setComposeTaskId(taskId));

        // 2. 异步执行合成：通过代理调用，使 @Async 生效
        getSelf().executeCompose(project.getId(), taskId, request);
    }

    /**
     * 执行合成：成功后转存到文件服务，记录成片实际的画幅、镜头数；
     * 最终回写以“仍为合成中、且仍是本次的任务编号”为条件，超时、被标记失败后不再覆盖，被标记失败后又重新合成时（包括转存期间），也不会结束新的合成
     *
     * @param projectId 项目编号
     * @param taskId 合成任务编号
     * @param request 合成参数
     */
    @Async
    public void executeCompose(Long projectId, String taskId, ComposeRequest request) {
        Integer whereStatus = AiDramaStatusEnum.IN_PROGRESS.getStatus();
        try {
            // 1. 调用 FFmpeg 合成
            ComposeResult result = AiFfmpegUtils.compose(ffmpegPath, ffprobePath, fontFile, request);

            // 2. 转存到文件服务：不指定文件名，按内容摘要命名，重新合成不会覆盖之前的成片地址
            String fileUrl = fileApi.createFile(result.getContent());

            // 3. 回写成功：记录成片实际使用的画幅、镜头数
            projectMapper.updateByIdAndComposeStatusAndTaskId(projectId, whereStatus, taskId, new AiDramaProjectDO()
                    .setComposeStatus(AiDramaStatusEnum.SUCCESS.getStatus())
                    .setComposeFileUrl(fileUrl).setComposeDuration(result.getDuration())
                    .setComposeAspectRatio(request.getAspectRatio()).setComposeStoryboardCount(request.getVideoUrls().size()));
        } catch (Exception e) {
            // 4. 回写失败
            log.error("[executeCompose][project({}) 合成失败]", projectId, e);
            projectMapper.updateByIdAndComposeStatusAndTaskId(projectId, whereStatus, taskId, new AiDramaProjectDO()
                    .setComposeStatus(AiDramaStatusEnum.FAIL.getStatus())
                    .setComposeErrorMessage(StrUtil.blankToDefault(e.getMessage(), "合成失败")));
        }
    }

    /**
     * 校验成片不在合成中：合成中的不能重复提交
     *
     * @param project 项目
     */
    private static void validateComposeNotRunning(AiDramaProjectDO project) {
        if (AiDramaStatusEnum.isRunning(project.getComposeStatus())) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    /**
     * 校验镜头视频已完成，且有视频地址
     *
     * @param storyboard 镜头
     */
    private static void validateStoryboardSuccess(AiDramaStoryboardDO storyboard) {
        if (ObjUtil.notEqual(storyboard.getVideoStatus(), AiDramaStatusEnum.SUCCESS.getStatus())
                || StrUtil.isEmpty(storyboard.getVideoUrl())) {
            throw exception(DRAMA_COMPOSE_STORYBOARD_NOT_SUCCESS,
                    StrUtil.blankToDefault(storyboard.getTitle(), String.valueOf(storyboard.getId())));
        }
    }

    // ========== 公开广场 ==========

    @Override
    public PageResult<AiDramaProjectDO> getProjectPublicPage(AiDramaProjectPublicPageReqVO pageReqVO) {
        return projectMapper.selectPageByPublic(pageReqVO);
    }

    @Override
    public AiDramaProjectDO getProjectPublic(Long id) {
        AiDramaProjectDO project = projectMapper.selectById(id);
        if (project == null || Boolean.FALSE.equals(project.getPublicStatus())
                || ObjUtil.notEqual(project.getComposeStatus(), AiDramaStatusEnum.SUCCESS.getStatus())) {
            throw exception(DRAMA_PROJECT_NOT_EXISTS);
        }
        return project;
    }

    // ========== 管理 ==========

    @Override
    public PageResult<AiDramaProjectDO> getProjectPage(AiDramaProjectPageReqVO pageReqVO) {
        return projectMapper.selectPage(pageReqVO);
    }

    @Override
    public AiDramaProjectDO getProject(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public AiDramaProjectDO validateProjectExists(Long id) {
        AiDramaProjectDO project = projectMapper.selectById(id);
        if (project == null) {
            throw exception(DRAMA_PROJECT_NOT_EXISTS);
        }
        return project;
    }

    @Override
    public List<AiDramaProjectDO> getProjectList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return projectMapper.selectByIds(ids);
    }

    @Override
    public List<AiDramaProjectDO> getProjectListByUserId(Long userId) {
        return projectMapper.selectListByUserId(userId);
    }

    @Override
    public void updateProjectPublicStatus(AiDramaProjectUpdatePublicReqVO updateReqVO) {
        AiDramaProjectDO project = validateProjectExists(updateReqVO.getId());
        // 只有成片已完成才能公开；取消公开不限制
        if (Boolean.TRUE.equals(updateReqVO.getPublicStatus())
                && ObjUtil.notEqual(project.getComposeStatus(), AiDramaStatusEnum.SUCCESS.getStatus())) {
            throw exception(DRAMA_PROJECT_PUBLIC_NOT_ALLOWED);
        }
        projectMapper.updateById(new AiDramaProjectDO().setId(project.getId()).setPublicStatus(updateReqVO.getPublicStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        // 1. 校验存在
        validateProjectExists(id);

        // 2.1 删除资产：角色、角色形象、场景、视觉资产、音频
        assetService.deleteAssetListByProjectId(id);
        // 2.2 删除分镜、剧本
        storyboardService.deleteStoryboardListByProjectId(id);
        scriptService.deleteScriptByProjectId(id);
        // 2.3 删除项目
        projectMapper.deleteById(id);
    }

    @Override
    public void recomposeProject(Long id) {
        // 1.1 校验项目：合成中的不能重复提交
        AiDramaProjectDO project = validateProjectExists(id);
        validateComposeNotRunning(project);
        // 1.2 使用全部已完成的镜头：至少 2 个
        List<AiDramaStoryboardDO> storyboards = filterList(storyboardService.getStoryboardList(id),
                storyboard -> Objects.equals(storyboard.getVideoStatus(), AiDramaStatusEnum.SUCCESS.getStatus())
                        && StrUtil.isNotEmpty(storyboard.getVideoUrl()));
        if (storyboards.size() < 2) {
            throw exception(DRAMA_COMPOSE_STORYBOARD_NOT_SUCCESS, "已完成的镜头不足 2 个");
        }

        // 2. 使用项目当前的画幅与转场合成；不含旁白、水印
        startCompose(project, new ComposeRequest()
                .setVideoUrls(convertList(storyboards, storyboard -> fileApi.presignGetUrlQuietly(storyboard.getVideoUrl())))
                .setAspectRatio(StrUtil.blankToDefault(project.getAspectRatio(), AiDramaAspectRatioEnum.RATIO_9_16.getRatio()))
                .setTransitionType(AiDramaTransitionTypeEnum.of(project.getComposeTransitionType()))
                .setTransitionDuration(ObjUtil.defaultIfNull(project.getComposeTransitionDurationSeconds(),
                        DEFAULT_TRANSITION_DURATION)));
    }

    // ========== 生成任务 ==========

    @Override
    public PageResult<AiDramaProjectDO> getComposeTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                           Collection<Long> projectIds) {
        return projectMapper.selectTaskPage(pageParam, reqVO, projectIds);
    }

    @Override
    public void updateComposeFail(Long id, String errorMessage) {
        // 1. 校验合成中
        AiDramaProjectDO project = validateProjectExists(id);
        if (!AiDramaStatusEnum.isRunning(project.getComposeStatus())) {
            throw exception(DRAMA_TASK_NOT_RUNNING);
        }

        // 2. 以读取到的状态、任务编号为条件更新：期间已结束或已重新合成时，不结束新的一轮
        projectMapper.updateByIdAndComposeStatusAndTaskId(id, project.getComposeStatus(), project.getComposeTaskId(),
                new AiDramaProjectDO().setComposeStatus(AiDramaStatusEnum.FAIL.getStatus()).setComposeErrorMessage(errorMessage));
    }

    @Override
    public int timeoutCompose(LocalDateTime updateTime, String errorMessage) {
        int count = 0;
        // 以查询到的状态、任务编号为条件更新：期间已结束或已重新合成时，不结束新的一轮
        for (AiDramaProjectDO project : projectMapper.selectListByComposeStatusAndUpdateTimeLt(
                AiDramaStatusEnum.IN_PROGRESS.getStatus(), updateTime)) {
            count += projectMapper.updateByIdAndComposeStatusAndTaskId(project.getId(), project.getComposeStatus(),
                    project.getComposeTaskId(), new AiDramaProjectDO().setComposeStatus(AiDramaStatusEnum.FAIL.getStatus())
                            .setComposeErrorMessage(errorMessage));
        }
        return count;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     */
    private AiDramaProjectServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
