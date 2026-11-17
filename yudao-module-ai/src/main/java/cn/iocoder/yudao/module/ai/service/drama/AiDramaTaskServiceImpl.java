package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaVisualAssetKindEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧生成任务 Service 实现类
 *
 * 没有独立的任务表，由角色形象、场景、视觉资产、分镜、项目（成片）上的状态汇总而来
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiDramaTaskServiceImpl implements AiDramaTaskService {

    /**
     * 超时时间（分钟）
     */
    private static final int TIMEOUT_MINUTES = 60;
    /**
     * 管理员标记失败的原因
     */
    private static final String UPDATE_FAIL_MESSAGE = "管理员标记失败";
    /**
     * 超时的失败原因
     */
    private static final String TIMEOUT_MESSAGE = "任务超时";

    @Resource
    private AiDramaProjectService projectService;
    @Resource
    private AiDramaAssetService assetService;
    @Resource
    private AiDramaStoryboardService storyboardService;

    // ========== 分页 ==========

    @Override
    public PageResult<AiDramaTaskRespVO> getTaskPage(AiDramaTaskPageReqVO pageReqVO) {
        // 1. 用户、项目筛选，转换为项目编号；为空表示不筛选
        Set<Long> projectIds = null;
        if (pageReqVO.getUserId() != null) {
            projectIds = convertSet(projectService.getProjectListByUserId(pageReqVO.getUserId()), AiDramaProjectDO::getId);
        }
        if (pageReqVO.getProjectId() != null) {
            projectIds = projectIds == null || projectIds.contains(pageReqVO.getProjectId())
                    ? Collections.singleton(pageReqVO.getProjectId()) : Collections.emptySet();
        }
        if (projectIds != null && projectIds.isEmpty()) {
            return PageResult.empty();
        }

        // 2.1 指定类型：直接分页
        PageResult<AiDramaTaskRespVO> pageResult;
        AiDramaTaskTypeEnum type = AiDramaTaskTypeEnum.valueOfType(pageReqVO.getType());
        if (type != null) {
            pageResult = getTaskPage(type, pageReqVO, pageReqVO, projectIds);
        } else {
            // 2.2 全部类型：每类取前 pageNo * pageSize 条，合并后按更新时间倒序再分页
            PageParam topParam = new PageParam().setPageNo(1).setPageSize(pageReqVO.getPageNo() * pageReqVO.getPageSize());
            List<AiDramaTaskRespVO> list = new ArrayList<>();
            long total = 0;
            for (AiDramaTaskTypeEnum item : AiDramaTaskTypeEnum.values()) {
                PageResult<AiDramaTaskRespVO> page = getTaskPage(item, topParam, pageReqVO, projectIds);
                list.addAll(page.getList());
                total += page.getTotal();
            }
            list.sort(Comparator.comparing(AiDramaTaskRespVO::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            int from = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
            pageResult = new PageResult<>(from >= list.size() ? new ArrayList<>()
                    : new ArrayList<>(list.subList(from, Math.min(from + pageReqVO.getPageSize(), list.size()))), total);
        }

        // 3. 填充项目名称、用户编号，是否超时
        Map<Long, AiDramaProjectDO> projectMap = convertMap(projectService.getProjectList(
                convertSet(pageResult.getList(), AiDramaTaskRespVO::getProjectId)), AiDramaProjectDO::getId);
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        pageResult.getList().forEach(task -> {
            AiDramaProjectDO project = projectMap.get(task.getProjectId());
            if (project != null) {
                task.setProjectName(project.getName()).setUserId(project.getUserId());
            }
            task.setTimeout(AiDramaStatusEnum.isRunning(task.getStatus())
                    && task.getUpdateTime() != null && task.getUpdateTime().isBefore(timeoutTime));
        });
        return pageResult;
    }

    /**
     * 获得一类任务的分页：按类型查询对应的记录，转换为任务
     *
     * @param type 任务类型
     * @param pageParam 分页参数
     * @param reqVO 筛选条件
     * @param projectIds 项目编号；为空表示不过滤
     * @return 分页
     */
    private PageResult<AiDramaTaskRespVO> getTaskPage(AiDramaTaskTypeEnum type, PageParam pageParam,
                                                      AiDramaTaskPageReqVO reqVO, Set<Long> projectIds) {
        switch (type) {
            case APPEARANCE: {
                PageResult<AiDramaCharacterAppearanceDO> page = assetService.getAppearanceTaskPage(pageParam, reqVO, projectIds);
                Map<Long, AiDramaCharacterDO> characterMap = convertMap(assetService.getCharacterList(
                        convertSet(page.getList(), AiDramaCharacterAppearanceDO::getCharacterId)), AiDramaCharacterDO::getId);
                return new PageResult<>(convertList(page.getList(), appearance -> {
                    AiDramaCharacterDO character = characterMap.get(appearance.getCharacterId());
                    return buildTask(type, appearance.getId(), character != null ? character.getProjectId() : null,
                            (character != null ? character.getName() : "") + " · 形象 " + appearance.getAppearanceIndex(),
                            appearance.getStatus(), appearance.getErrorMessage(), CollUtil.getLast(appearance.getImageDescriptions()),
                            appearance.getModelId(), appearance.getTaskId(),
                            getSelectedUrl(appearance.getImageUrls(), appearance.getSelectedIndex()),
                            appearance.getCreateTime(), appearance.getUpdateTime());
                }), page.getTotal());
            }
            case LOCATION: {
                PageResult<AiDramaLocationDO> page = assetService.getLocationTaskPage(pageParam, reqVO, projectIds);
                return new PageResult<>(convertList(page.getList(), location -> buildTask(type, location.getId(),
                        location.getProjectId(), location.getName(), location.getStatus(), location.getErrorMessage(),
                        CollUtil.getLast(location.getImageDescriptions()), location.getModelId(), location.getTaskId(),
                        getSelectedUrl(location.getImageUrls(), location.getSelectedIndex()),
                        location.getCreateTime(), location.getUpdateTime())), page.getTotal());
            }
            case VISUAL_ASSET: {
                PageResult<AiDramaVisualAssetDO> page = assetService.getVisualAssetTaskPage(pageParam, reqVO, projectIds);
                return new PageResult<>(convertList(page.getList(), visualAsset -> buildTask(type, visualAsset.getId(),
                        visualAsset.getProjectId(), (ObjUtil.equal(visualAsset.getKind(), AiDramaVisualAssetKindEnum.SHOT_FRAME.getType())
                                ? "关键帧 · " : "道具 · ") + ObjUtil.defaultIfNull(visualAsset.getTitle(), ""),
                        visualAsset.getStatus(), visualAsset.getErrorMessage(),
                        visualAsset.getPrompt(), visualAsset.getModelId(), visualAsset.getTaskId(), visualAsset.getImageUrl(),
                        visualAsset.getCreateTime(), visualAsset.getUpdateTime())), page.getTotal());
            }
            case STORYBOARD: {
                PageResult<AiDramaStoryboardDO> page = storyboardService.getStoryboardTaskPage(pageParam, reqVO, projectIds);
                return new PageResult<>(convertList(page.getList(), storyboard -> buildTask(type, storyboard.getId(),
                        storyboard.getProjectId(), "第 " + storyboard.getSceneNo() + " 镜 · " + ObjUtil.defaultIfNull(storyboard.getTitle(), ""),
                        storyboard.getVideoStatus(), storyboard.getErrorMessage(), storyboard.getVideoPrompt(),
                        storyboard.getVideoModelId(), storyboard.getTaskId(), storyboard.getVideoUrl(),
                        storyboard.getCreateTime(), storyboard.getUpdateTime())), page.getTotal());
            }
            case COMPOSE: {
                if (reqVO.getModelId() != null) {
                    return PageResult.empty();
                }
                PageResult<AiDramaProjectDO> page = projectService.getComposeTaskPage(pageParam, reqVO, projectIds);
                return new PageResult<>(convertList(page.getList(), project -> buildTask(type, project.getId(),
                        project.getId(), "成片", project.getComposeStatus(), project.getComposeErrorMessage(), null,
                        null, null, project.getComposeFileUrl(), project.getCreateTime(), project.getUpdateTime())), page.getTotal());
            }
            default:
                return PageResult.empty();
        }
    }

    /**
     * 构建任务
     */
    private static AiDramaTaskRespVO buildTask(AiDramaTaskTypeEnum type, Long id, Long projectId, String title,
                                               Integer status, String errorMessage, String prompt, Long modelId,
                                               String taskId, String resultUrl, LocalDateTime createTime,
                                               LocalDateTime updateTime) {
        return new AiDramaTaskRespVO().setType(type.getType()).setId(id).setProjectId(projectId).setTitle(title)
                .setStatus(status).setErrorMessage(errorMessage).setPrompt(prompt).setModelId(modelId)
                .setTaskId(taskId).setResultUrl(resultUrl).setCreateTime(createTime).setUpdateTime(updateTime);
    }

    /**
     * 获得任务的结果图：选用的候选图，未选用时取最后一张
     */
    private static String getSelectedUrl(List<String> imageUrls, Integer selectedIndex) {
        if (CollUtil.isEmpty(imageUrls)) {
            return null;
        }
        if (selectedIndex != null && selectedIndex >= 0 && selectedIndex < imageUrls.size()) {
            return imageUrls.get(selectedIndex);
        }
        return CollUtil.getLast(imageUrls);
    }

    // ========== 操作 ==========

    @Override
    public void resubmitTask(AiDramaTaskActionReqVO reqVO) {
        switch (AiDramaTaskTypeEnum.valueOfType(reqVO.getType())) {
            case APPEARANCE:
                assetService.resubmitAppearanceImage(reqVO.getId());
                break;
            case LOCATION:
                assetService.resubmitLocationImage(reqVO.getId());
                break;
            case VISUAL_ASSET: {
                // 关键帧：镜头视频生成中不能重新出图，视频的查询依赖起始帧
                AiDramaVisualAssetDO visualAsset = assetService.getVisualAsset(reqVO.getId());
                if (visualAsset != null && visualAsset.getStoryboardId() != null) {
                    storyboardService.validateStoryboardVideoNotRunning(visualAsset.getStoryboardId());
                }
                assetService.resubmitVisualAssetImage(reqVO.getId());
                break;
            }
            case STORYBOARD:
                storyboardService.resubmitStoryboardVideo(reqVO.getId());
                break;
            case COMPOSE: {
                AiDramaProjectDO project = projectService.validateProjectExists(reqVO.getId());
                if (ObjUtil.notEqual(project.getComposeStatus(), AiDramaStatusEnum.FAIL.getStatus())) {
                    throw exception(DRAMA_TASK_NOT_FAIL);
                }
                projectService.recomposeProject(reqVO.getId());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void syncTask(AiDramaTaskActionReqVO reqVO) {
        if (AiDramaTaskTypeEnum.valueOfType(reqVO.getType()) != AiDramaTaskTypeEnum.STORYBOARD) {
            throw exception(DRAMA_TASK_SYNC_NOT_SUPPORTED);
        }
        storyboardService.syncStoryboardVideo(reqVO.getId());
    }

    @Override
    public void updateTaskFail(AiDramaTaskActionReqVO reqVO) {
        switch (AiDramaTaskTypeEnum.valueOfType(reqVO.getType())) {
            case APPEARANCE:
                assetService.updateAppearanceFail(reqVO.getId(), UPDATE_FAIL_MESSAGE);
                break;
            case LOCATION:
                assetService.updateLocationFail(reqVO.getId(), UPDATE_FAIL_MESSAGE);
                break;
            case VISUAL_ASSET:
                assetService.updateVisualAssetFail(reqVO.getId(), UPDATE_FAIL_MESSAGE);
                break;
            case STORYBOARD:
                storyboardService.updateStoryboardVideoFail(reqVO.getId(), UPDATE_FAIL_MESSAGE);
                break;
            case COMPOSE:
                projectService.updateComposeFail(reqVO.getId(), UPDATE_FAIL_MESSAGE);
                break;
            default:
                break;
        }
    }

    // ========== 超时 ==========

    @Override
    public int timeoutTasks() {
        LocalDateTime updateTime = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        // 1. 镜头视频：待处理、生成中、转存中
        int count = storyboardService.timeoutStoryboardVideo(updateTime, TIMEOUT_MESSAGE);
        // 2. 出图：生成中（例如服务重启，异步任务丢失）
        count += assetService.timeoutImage(updateTime, TIMEOUT_MESSAGE);
        // 3. 成片：合成中
        count += projectService.timeoutCompose(updateTime, TIMEOUT_MESSAGE);
        return count;
    }

}
