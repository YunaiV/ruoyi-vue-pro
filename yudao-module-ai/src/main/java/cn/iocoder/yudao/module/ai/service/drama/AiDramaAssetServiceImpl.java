package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.*;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiImageInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiImageUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.annotations.VisibleForTesting;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧资产 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiDramaAssetServiceImpl implements AiDramaAssetService {

    /**
     * 视觉资产类型：道具
     */
    public static final String VISUAL_ASSET_KIND_PROP = "prop";

    /**
     * 通用绘画模型的出图尺寸
     */
    private static final int IMAGE_SIZE = 1024;
    /**
     * Seedream 的出图尺寸
     */
    private static final String SEEDREAM_SIZE = "2048x2048";

    @Resource
    private AiDramaCharacterMapper characterMapper;
    @Resource
    private AiDramaCharacterAppearanceMapper appearanceMapper;
    @Resource
    private AiDramaLocationMapper locationMapper;
    @Resource
    private AiDramaVisualAssetMapper visualAssetMapper;
    @Resource
    private AiDramaAudioMapper audioMapper;

    @Resource
    private AiDramaProjectService projectService;
    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private FileApi fileApi;

    // ========== 角色 ==========

    @Override
    public List<AiDramaCharacterDO> getCharacterList(Long projectId) {
        return characterMapper.selectListByProjectId(projectId);
    }

    @Override
    public List<AiDramaCharacterDO> getCharacterList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return characterMapper.selectByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCharacterMy(Long userId, AiDramaCharacterSaveReqVO createReqVO) {
        // 校验项目
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);
        // 插入
        return createCharacter(BeanUtils.toBean(createReqVO, AiDramaCharacterDO.class, character -> character.setId(null)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCharacter(AiDramaCharacterDO character) {
        // 插入角色，及默认的第一个形象
        characterMapper.insert(character);
        appearanceMapper.insert(new AiDramaCharacterAppearanceDO().setCharacterId(character.getId())
                .setAppearanceIndex(1).setCandidates(new ArrayList<>()));
        return character.getId();
    }

    @Override
    public void updateCharacterMy(Long userId, AiDramaCharacterSaveReqVO updateReqVO) {
        // 校验存在
        validateCharacterMy(updateReqVO.getId(), userId);
        // 更新：不修改所属项目
        characterMapper.updateById(BeanUtils.toBean(updateReqVO, AiDramaCharacterDO.class,
                character -> character.setProjectId(null)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCharacterMy(Long userId, Long id) {
        // 校验存在
        validateCharacterMy(id, userId);
        // 删除：角色形象、样音一并删除
        appearanceMapper.deleteByCharacterIds(Collections.singleton(id));
        audioMapper.deleteByCharacterId(id);
        characterMapper.deleteById(id);
    }

    /**
     * 校验角色存在，且属于该用户的项目
     */
    private AiDramaCharacterDO validateCharacterMy(Long id, Long userId) {
        AiDramaCharacterDO character = characterMapper.selectById(id);
        if (character == null) {
            throw exception(DRAMA_CHARACTER_NOT_EXISTS);
        }
        projectService.validateProjectMy(character.getProjectId(), userId);
        return character;
    }

    // ========== 角色形象 ==========

    @Override
    public List<AiDramaCharacterAppearanceDO> getAppearanceListByCharacterIds(Collection<Long> characterIds) {
        if (CollUtil.isEmpty(characterIds)) {
            return Collections.emptyList();
        }
        return appearanceMapper.selectListByCharacterIds(characterIds);
    }

    @Override
    public AiDramaCharacterAppearanceDO getAppearance(Long id) {
        return appearanceMapper.selectById(id);
    }

    @Override
    public List<AiDramaCharacterAppearanceDO> getAppearanceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return appearanceMapper.selectByIds(ids);
    }

    @Override
    public Long createAppearanceMy(Long userId, AiDramaAppearanceSaveReqVO createReqVO) {
        // 1. 校验角色
        validateCharacterMy(createReqVO.getCharacterId(), userId);

        // 2. 插入：序号排在已有形象之后，候选图为空
        int maxIndex = appearanceMapper.selectListByCharacterId(createReqVO.getCharacterId()).stream()
                .mapToInt(AiDramaCharacterAppearanceDO::getAppearanceIndex).max().orElse(0);
        AiDramaCharacterAppearanceDO appearance = BeanUtils.toBean(createReqVO, AiDramaCharacterAppearanceDO.class,
                o -> o.setId(null).setSelectedIndex(null).setAppearanceIndex(maxIndex + 1).setCandidates(new ArrayList<>()));
        appearanceMapper.insert(appearance);
        return appearance.getId();
    }

    @Override
    public void updateAppearanceMy(Long userId, AiDramaAppearanceSaveReqVO updateReqVO) {
        // 校验存在，且选用的候选图有效
        AiDramaCharacterAppearanceDO appearance = validateAppearanceMy(updateReqVO.getId(), userId);
        validateSelectedIndex(appearance.getCandidates(), updateReqVO.getSelectedIndex());
        // 更新：不修改所属角色
        appearanceMapper.updateById(BeanUtils.toBean(updateReqVO, AiDramaCharacterAppearanceDO.class,
                o -> o.setCharacterId(null)));
    }

    @Override
    public void deleteAppearanceMy(Long userId, Long id) {
        // 校验存在
        validateAppearanceMy(id, userId);
        // 删除
        appearanceMapper.deleteById(id);
    }

    @Override
    public void generateAppearanceImageMy(Long userId, AiDramaImageGenerateReqVO reqVO) {
        // 校验存在
        AiDramaCharacterAppearanceDO appearance = validateAppearanceMy(reqVO.getId(), userId);
        // 提交出图
        submitAppearanceImage(appearance, buildImageClient(reqVO.getModelId()));
    }

    @Override
    public void resubmitAppearanceImage(Long id) {
        // 校验存在，且已失败
        AiDramaCharacterAppearanceDO appearance = appearanceMapper.selectById(id);
        if (appearance == null) {
            throw exception(DRAMA_APPEARANCE_NOT_EXISTS);
        }
        validateStatusFail(appearance.getStatus());
        // 使用上次的绘画模型，重新出图
        submitAppearanceImage(appearance, buildImageClient(getLastModelId(appearance.getCandidates())));
    }

    @Override
    public PageResult<AiDramaCharacterAppearanceDO> getAppearanceTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                                          Collection<Long> projectIds) {
        // 角色形象挂在角色下：项目筛选转换为角色筛选
        Set<Long> characterIds = null;
        if (projectIds != null) {
            characterIds = convertSet(characterMapper.selectListByProjectIds(projectIds), AiDramaCharacterDO::getId);
            if (characterIds.isEmpty()) {
                return PageResult.empty();
            }
        }
        return appearanceMapper.selectTaskPage(pageParam, reqVO, characterIds);
    }

    @Override
    public void updateAppearanceFail(Long id, String errorMessage) {
        // 1. 校验生成中
        AiDramaCharacterAppearanceDO appearance = appearanceMapper.selectById(id);
        if (appearance == null) {
            throw exception(DRAMA_APPEARANCE_NOT_EXISTS);
        }
        validateStatusRunning(appearance.getStatus());

        // 2. 以读取到的状态、任务编号为条件更新：期间已结束或已重新提交时，不结束新的一轮
        appearanceMapper.updateByIdAndStatusAndTaskId(id, appearance.getStatus(), appearance.getTaskId(),
                new AiDramaCharacterAppearanceDO().setStatus(AiDramaStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage));
    }

    /**
     * 校验角色形象存在，且属于该用户的项目
     */
    private AiDramaCharacterAppearanceDO validateAppearanceMy(Long id, Long userId) {
        AiDramaCharacterAppearanceDO appearance = appearanceMapper.selectById(id);
        if (appearance == null) {
            throw exception(DRAMA_APPEARANCE_NOT_EXISTS);
        }
        validateCharacterMy(appearance.getCharacterId(), userId);
        return appearance;
    }

    /**
     * 提交角色形象出图：追加占位候选、置为生成中，再异步出图
     */
    private void submitAppearanceImage(AiDramaCharacterAppearanceDO appearance, ImageClient client) {
        // 1. 校验不在生成中，拼接提示词
        validateStatusNotRunning(appearance.getStatus());
        AiDramaCharacterDO character = characterMapper.selectById(appearance.getCharacterId());
        AiDramaProjectDO project = projectService.validateProjectExists(character.getProjectId());
        String prompt = buildImagePrompt(project.getStyle(), "人物设定图，单人，全身，纯色背景",
                character.getName(), firstNotBlank(character.getPrompt(), character.getDescription()),
                firstNotBlank(appearance.getPrompt(), appearance.getDescription()));
        // 2. 追加占位候选（地址为空），记录本次的提示词与模型；上次失败留下的占位先去掉；记录本次的出图任务编号
        String taskId = IdUtil.fastSimpleUUID();
        appearanceMapper.updateById(new AiDramaCharacterAppearanceDO().setId(appearance.getId())
                .setCandidates(appendPlaceholder(appearance.getCandidates(), prompt, client.model().getId()))
                .setTaskId(taskId).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        // 3. 异步出图：通过代理调用，使 @Async 生效
        getSelf().executeImage(AiDramaTaskTypeEnum.APPEARANCE, appearance.getId(), taskId, client, prompt,
                buildReferenceUrls(character.getReferenceImageUrl()));
    }

    // ========== 场景 ==========

    @Override
    public List<AiDramaLocationDO> getLocationList(Long projectId) {
        return locationMapper.selectListByProjectId(projectId);
    }

    @Override
    public AiDramaLocationDO getLocation(Long id) {
        return locationMapper.selectById(id);
    }

    @Override
    public Long createLocationMy(Long userId, AiDramaLocationSaveReqVO createReqVO) {
        // 校验项目
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);
        // 插入：候选图为空
        return createLocation(BeanUtils.toBean(createReqVO, AiDramaLocationDO.class,
                location -> location.setId(null).setSelectedIndex(null).setCandidates(new ArrayList<>())));
    }

    @Override
    public Long createLocation(AiDramaLocationDO location) {
        locationMapper.insert(location);
        return location.getId();
    }

    @Override
    public void updateLocationMy(Long userId, AiDramaLocationSaveReqVO updateReqVO) {
        // 校验存在，且选用的候选图有效
        AiDramaLocationDO location = validateLocationMy(updateReqVO.getId(), userId);
        validateSelectedIndex(location.getCandidates(), updateReqVO.getSelectedIndex());
        // 更新：不修改所属项目
        locationMapper.updateById(BeanUtils.toBean(updateReqVO, AiDramaLocationDO.class, o -> o.setProjectId(null)));
    }

    @Override
    public void deleteLocationMy(Long userId, Long id) {
        // 校验存在
        validateLocationMy(id, userId);
        // 删除
        locationMapper.deleteById(id);
    }

    @Override
    public void generateLocationImageMy(Long userId, AiDramaImageGenerateReqVO reqVO) {
        // 校验存在
        AiDramaLocationDO location = validateLocationMy(reqVO.getId(), userId);
        // 提交出图
        submitLocationImage(location, buildImageClient(reqVO.getModelId()));
    }

    @Override
    public void resubmitLocationImage(Long id) {
        // 校验存在，且已失败
        AiDramaLocationDO location = locationMapper.selectById(id);
        if (location == null) {
            throw exception(DRAMA_LOCATION_NOT_EXISTS);
        }
        validateStatusFail(location.getStatus());
        // 使用上次的绘画模型，重新出图
        submitLocationImage(location, buildImageClient(getLastModelId(location.getCandidates())));
    }

    @Override
    public PageResult<AiDramaLocationDO> getLocationTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                             Collection<Long> projectIds) {
        return locationMapper.selectTaskPage(pageParam, reqVO, projectIds);
    }

    @Override
    public void updateLocationFail(Long id, String errorMessage) {
        // 1. 校验生成中
        AiDramaLocationDO location = locationMapper.selectById(id);
        if (location == null) {
            throw exception(DRAMA_LOCATION_NOT_EXISTS);
        }
        validateStatusRunning(location.getStatus());

        // 2. 以读取到的状态、任务编号为条件更新：期间已结束或已重新提交时，不结束新的一轮
        locationMapper.updateByIdAndStatusAndTaskId(id, location.getStatus(), location.getTaskId(),
                new AiDramaLocationDO().setStatus(AiDramaStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage));
    }

    /**
     * 校验场景存在，且属于该用户的项目
     */
    private AiDramaLocationDO validateLocationMy(Long id, Long userId) {
        AiDramaLocationDO location = locationMapper.selectById(id);
        if (location == null) {
            throw exception(DRAMA_LOCATION_NOT_EXISTS);
        }
        projectService.validateProjectMy(location.getProjectId(), userId);
        return location;
    }

    /**
     * 提交场景出图：追加占位候选、置为生成中，再异步出图
     */
    private void submitLocationImage(AiDramaLocationDO location, ImageClient client) {
        // 1. 校验不在生成中，拼接提示词
        validateStatusNotRunning(location.getStatus());
        AiDramaProjectDO project = projectService.validateProjectExists(location.getProjectId());
        String prompt = buildImagePrompt(project.getStyle(), "场景概念图，无人物",
                location.getName(), firstNotBlank(location.getPrompt(), location.getDescription()));
        // 2. 追加占位候选，置为生成中
        String taskId = IdUtil.fastSimpleUUID();
        locationMapper.updateById(new AiDramaLocationDO().setId(location.getId())
                .setCandidates(appendPlaceholder(location.getCandidates(), prompt, client.model().getId()))
                .setTaskId(taskId).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        // 3. 异步出图
        getSelf().executeImage(AiDramaTaskTypeEnum.LOCATION, location.getId(), taskId, client, prompt,
                buildReferenceUrls(location.getReferenceImageUrl()));
    }

    // ========== 视觉资产 ==========

    @Override
    public List<AiDramaVisualAssetDO> getVisualAssetList(Long projectId) {
        return visualAssetMapper.selectListByProjectId(projectId);
    }

    @Override
    public AiDramaVisualAssetDO getVisualAsset(Long id) {
        return visualAssetMapper.selectById(id);
    }

    @Override
    public List<AiDramaVisualAssetDO> getVisualAssetList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return visualAssetMapper.selectByIds(ids);
    }

    @Override
    public Long createVisualAssetMy(Long userId, AiDramaVisualAssetSaveReqVO createReqVO) {
        // 校验项目
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);
        // 插入：类型为空时默认道具
        return createVisualAsset(BeanUtils.toBean(createReqVO, AiDramaVisualAssetDO.class, visualAsset -> visualAsset
                .setId(null).setKind(StrUtil.blankToDefault(createReqVO.getKind(), VISUAL_ASSET_KIND_PROP))));
    }

    @Override
    public Long createVisualAsset(AiDramaVisualAssetDO visualAsset) {
        visualAssetMapper.insert(visualAsset);
        return visualAsset.getId();
    }

    @Override
    public void updateVisualAssetMy(Long userId, AiDramaVisualAssetSaveReqVO updateReqVO) {
        // 校验存在
        validateVisualAssetMy(updateReqVO.getId(), userId);
        // 更新：不修改所属项目，类型为空时默认道具
        visualAssetMapper.updateById(BeanUtils.toBean(updateReqVO, AiDramaVisualAssetDO.class, visualAsset -> visualAsset
                .setProjectId(null).setKind(StrUtil.blankToDefault(updateReqVO.getKind(), VISUAL_ASSET_KIND_PROP))));
    }

    @Override
    public void deleteVisualAssetMy(Long userId, Long id) {
        // 校验存在
        validateVisualAssetMy(id, userId);
        // 删除
        visualAssetMapper.deleteById(id);
    }

    @Override
    public void generateVisualAssetImageMy(Long userId, AiDramaImageGenerateReqVO reqVO) {
        // 校验存在
        AiDramaVisualAssetDO visualAsset = validateVisualAssetMy(reqVO.getId(), userId);
        // 提交出图
        submitVisualAssetImage(visualAsset, buildImageClient(reqVO.getModelId()));
    }

    @Override
    public void resubmitVisualAssetImage(Long id) {
        // 校验存在，且已失败
        AiDramaVisualAssetDO visualAsset = visualAssetMapper.selectById(id);
        if (visualAsset == null) {
            throw exception(DRAMA_VISUAL_ASSET_NOT_EXISTS);
        }
        validateStatusFail(visualAsset.getStatus());
        // 使用上次的绘画模型，重新出图
        submitVisualAssetImage(visualAsset, buildImageClient(visualAsset.getModelId()));
    }

    @Override
    public PageResult<AiDramaVisualAssetDO> getVisualAssetTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                                   Collection<Long> projectIds) {
        return visualAssetMapper.selectTaskPage(pageParam, reqVO, projectIds);
    }

    @Override
    public void updateVisualAssetFail(Long id, String errorMessage) {
        // 1. 校验生成中
        AiDramaVisualAssetDO visualAsset = visualAssetMapper.selectById(id);
        if (visualAsset == null) {
            throw exception(DRAMA_VISUAL_ASSET_NOT_EXISTS);
        }
        validateStatusRunning(visualAsset.getStatus());

        // 2. 以读取到的状态、任务编号为条件更新：期间已结束或已重新提交时，不结束新的一轮
        visualAssetMapper.updateByIdAndStatusAndTaskId(id, visualAsset.getStatus(), visualAsset.getTaskId(),
                new AiDramaVisualAssetDO().setStatus(AiDramaStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage));
    }

    /**
     * 校验视觉资产存在，且属于该用户的项目
     */
    private AiDramaVisualAssetDO validateVisualAssetMy(Long id, Long userId) {
        AiDramaVisualAssetDO visualAsset = visualAssetMapper.selectById(id);
        if (visualAsset == null) {
            throw exception(DRAMA_VISUAL_ASSET_NOT_EXISTS);
        }
        projectService.validateProjectMy(visualAsset.getProjectId(), userId);
        return visualAsset;
    }

    /**
     * 提交视觉资产出图：记录模型、置为生成中，再异步出图；不使用参考图
     */
    private void submitVisualAssetImage(AiDramaVisualAssetDO visualAsset, ImageClient client) {
        // 1. 校验不在生成中，拼接提示词
        validateStatusNotRunning(visualAsset.getStatus());
        AiDramaProjectDO project = projectService.validateProjectExists(visualAsset.getProjectId());
        String prompt = buildImagePrompt(project.getStyle(), "道具设定图，纯色背景",
                visualAsset.getTitle(), visualAsset.getPrompt());
        // 2. 置为生成中
        String taskId = IdUtil.fastSimpleUUID();
        visualAssetMapper.updateById(new AiDramaVisualAssetDO().setId(visualAsset.getId())
                .setModelId(client.model().getId()).setTaskId(taskId).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        // 3. 异步出图
        getSelf().executeImage(AiDramaTaskTypeEnum.VISUAL_ASSET, visualAsset.getId(), taskId, client, prompt,
                Collections.emptyList());
    }

    // ========== 一键补齐 ==========

    @Override
    public int generateMissingImagesMy(Long userId, AiDramaImageGenerateReqVO reqVO) {
        // 1. 校验项目、绘画模型
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getId(), userId);
        ImageClient client = buildImageClient(reqVO.getModelId());
        int count = 0;
        // 2.1 角色形象：没有任何成功的候选
        List<Long> characterIds = convertList(characterMapper.selectListByProjectId(project.getId()), AiDramaCharacterDO::getId);
        for (AiDramaCharacterAppearanceDO appearance : getAppearanceListByCharacterIds(characterIds)) {
            if (!AiDramaStatusEnum.isRunning(appearance.getStatus()) && !hasImage(appearance.getCandidates())) {
                submitAppearanceImage(appearance, client);
                count++;
            }
        }
        // 2.2 场景：没有任何成功的候选
        for (AiDramaLocationDO location : locationMapper.selectListByProjectId(project.getId())) {
            if (!AiDramaStatusEnum.isRunning(location.getStatus()) && !hasImage(location.getCandidates())) {
                submitLocationImage(location, client);
                count++;
            }
        }
        // 2.3 视觉资产：没有图片
        for (AiDramaVisualAssetDO visualAsset : visualAssetMapper.selectListByProjectId(project.getId())) {
            if (!AiDramaStatusEnum.isRunning(visualAsset.getStatus()) && StrUtil.isEmpty(visualAsset.getImageUrl())) {
                submitVisualAssetImage(visualAsset, client);
                count++;
            }
        }
        return count;
    }

    @Override
    public int timeoutImage(LocalDateTime updateTime, String errorMessage) {
        Integer inProgress = AiDramaStatusEnum.IN_PROGRESS.getStatus();
        Integer fail = AiDramaStatusEnum.FAIL.getStatus();
        int count = 0;
        // 以查询到的状态、任务编号为条件更新：期间已结束或已重新提交时，不结束新的一轮
        // 1. 角色形象
        for (AiDramaCharacterAppearanceDO appearance : appearanceMapper.selectListByStatusAndUpdateTimeLt(inProgress, updateTime)) {
            count += appearanceMapper.updateByIdAndStatusAndTaskId(appearance.getId(), appearance.getStatus(), appearance.getTaskId(),
                    new AiDramaCharacterAppearanceDO().setStatus(fail).setErrorMessage(errorMessage));
        }
        // 2. 场景
        for (AiDramaLocationDO location : locationMapper.selectListByStatusAndUpdateTimeLt(inProgress, updateTime)) {
            count += locationMapper.updateByIdAndStatusAndTaskId(location.getId(), location.getStatus(), location.getTaskId(),
                    new AiDramaLocationDO().setStatus(fail).setErrorMessage(errorMessage));
        }
        // 3. 视觉资产
        for (AiDramaVisualAssetDO visualAsset : visualAssetMapper.selectListByStatusAndUpdateTimeLt(inProgress, updateTime)) {
            count += visualAssetMapper.updateByIdAndStatusAndTaskId(visualAsset.getId(), visualAsset.getStatus(), visualAsset.getTaskId(),
                    new AiDramaVisualAssetDO().setStatus(fail).setErrorMessage(errorMessage));
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssetListByProjectId(Long projectId) {
        // 1. 角色形象挂在角色下，先按角色删除
        List<Long> characterIds = convertList(characterMapper.selectListByProjectId(projectId), AiDramaCharacterDO::getId);
        if (CollUtil.isNotEmpty(characterIds)) {
            appearanceMapper.deleteByCharacterIds(characterIds);
        }
        // 2. 角色、场景、视觉资产、音频
        characterMapper.deleteByProjectId(projectId);
        locationMapper.deleteByProjectId(projectId);
        visualAssetMapper.deleteByProjectId(projectId);
        audioMapper.deleteByProjectId(projectId);
    }

    // ========== 出图 ==========

    /**
     * 绘画客户端：Seedream 使用火山方舟，其它使用 Spring AI 的 ImageModel
     */
    public record ImageClient(AiModelDO model, ArkService arkService, ImageModel imageModel) {
    }

    /**
     * 校验绘画模型，并创建客户端：创建时会校验模型与密钥的平台一致
     *
     * Midjourney 不支持：没有同步出图的 ImageModel，需要通过 MidjourneyApi 提交后由 AiMidjourneySyncJob 轮询结果，
     * 与这里“一次调用直接拿到图片”的出图方式不同
     */
    private ImageClient buildImageClient(Long modelId) {
        if (modelId == null) {
            throw exception(DRAMA_TASK_RESUBMIT_NO_MODEL);
        }
        AiModelDO model = modelService.validateModel(modelId);
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.IMAGE.getType())) {
            throw exception(DRAMA_IMAGE_MODEL_ERROR, model.getName());
        }
        if (ObjUtil.equal(model.getPlatform(), AiPlatformEnum.MIDJOURNEY.getPlatform())) {
            throw exception(DRAMA_IMAGE_MODEL_ERROR, model.getName() + "（Midjourney 暂不支持）");
        }
        if (ObjUtil.equal(model.getPlatform(), AiPlatformEnum.BYTEDANCE.getPlatform())) {
            return new ImageClient(model, modelService.getArkService(model.getId()), null);
        }
        return new ImageClient(model, null, modelService.getImageModel(model.getId()));
    }

    /**
     * 异步出图：成功时转存到文件服务；最终回写以“仍为生成中、且仍是本次的任务编号”为条件，
     * 超时、被标记失败后不再覆盖，被标记失败后又重新提交时也不会结束新的任务
     */
    @Async
    public void executeImage(AiDramaTaskTypeEnum type, Long id, String taskId, ImageClient client, String prompt,
                             List<String> referenceUrls) {
        // 1. 出图
        String imageUrl = null;
        String errorMessage = null;
        try {
            imageUrl = drawImage(client, prompt, referenceUrls);
        } catch (Exception e) {
            log.error("[executeImage][type({}) id({}) 出图失败]", type.getType(), id, e);
            errorMessage = StrUtil.blankToDefault(e.getMessage(), "出图失败");
        }
        // 2. 回写结果
        Integer status = imageUrl != null ? AiDramaStatusEnum.SUCCESS.getStatus() : AiDramaStatusEnum.FAIL.getStatus();
        Integer whereStatus = AiDramaStatusEnum.IN_PROGRESS.getStatus();
        switch (type) {
            case APPEARANCE: {
                // 候选图在读取后修改：被重新提交时任务编号已变，下面的条件更新不会写入
                AiDramaCharacterAppearanceDO appearance = appearanceMapper.selectById(id);
                if (appearance == null) {
                    return;
                }
                List<AiDramaCharacterAppearanceDO.Candidate> candidates = fillPlaceholder(appearance.getCandidates(), imageUrl);
                appearanceMapper.updateByIdAndStatusAndTaskId(id, whereStatus, taskId, new AiDramaCharacterAppearanceDO()
                        .setCandidates(candidates).setStatus(status).setErrorMessage(errorMessage)
                        .setSelectedIndex(imageUrl != null && appearance.getSelectedIndex() == null ? candidates.size() - 1 : null));
                break;
            }
            case LOCATION: {
                AiDramaLocationDO location = locationMapper.selectById(id);
                if (location == null) {
                    return;
                }
                List<AiDramaCharacterAppearanceDO.Candidate> candidates = fillPlaceholder(location.getCandidates(), imageUrl);
                locationMapper.updateByIdAndStatusAndTaskId(id, whereStatus, taskId, new AiDramaLocationDO()
                        .setCandidates(candidates).setStatus(status).setErrorMessage(errorMessage)
                        .setSelectedIndex(imageUrl != null && location.getSelectedIndex() == null ? candidates.size() - 1 : null));
                break;
            }
            case VISUAL_ASSET:
                visualAssetMapper.updateByIdAndStatusAndTaskId(id, whereStatus, taskId, new AiDramaVisualAssetDO()
                        .setImageUrl(imageUrl).setStatus(status).setErrorMessage(errorMessage));
                break;
            default:
                break;
        }
    }

    /**
     * 调用绘画模型出图，转存到文件服务
     *
     * @return 文件地址
     */
    private String drawImage(ImageClient client, String prompt, List<String> referenceUrls) {
        AiModelDO model = client.model();
        // 1. Seedream：返回临时地址，参考图需要可公开访问
        if (client.arkService() != null) {
            AiImageDO image = new AiImageDO().setModel(model.getModel()).setPrompt(prompt)
                    .setSeedreamOptions(new AiImageDO.SeedreamOptions().setSize(SEEDREAM_SIZE))
                    .setInputImages(convertList(referenceUrls, url -> new AiImageDO.InputImage(
                            AiImageInputRoleEnum.REFERENCE.getRole(), fileApi.presignGetUrlQuietly(url))));
            ImagesResponse response = client.arkService().generateImages(AiImageUtils.buildSeedreamRequest(image));
            if (response.getError() != null) {
                throw new IllegalStateException(response.getError().getMessage());
            }
            ImagesResponse.Image item = CollUtil.getFirst(response.getData());
            if (item == null || StrUtil.isEmpty(item.getUrl())) {
                throw new IllegalStateException("生成结果为空");
            }
            return fileApi.createFile(HttpUtil.downloadBytes(item.getUrl()));
        }
        // 2. 其它：按正方形出图，返回 base64 或地址；不支持参考图
        ImageResponse response = client.imageModel().call(new ImagePrompt(prompt, AiImageUtils.buildImageOptions(
                AiPlatformEnum.validatePlatform(model.getPlatform()), model.getModel(), IMAGE_SIZE, IMAGE_SIZE)));
        if (response.getResult() == null) {
            throw new IllegalStateException("生成结果为空");
        }
        String b64Json = response.getResult().getOutput().getB64Json();
        return fileApi.createFile(StrUtil.isNotEmpty(b64Json) ? Base64.decode(b64Json)
                : HttpUtil.downloadBytes(response.getResult().getOutput().getUrl()));
    }

    /**
     * 拼接出图提示词：画风、用途、名称、设定，跳过空值
     */
    @VisibleForTesting
    static String buildImagePrompt(String style, String usage, String... parts) {
        List<String> items = new ArrayList<>();
        for (String part : parts) {
            if (StrUtil.isNotBlank(part)) {
                items.add(StrUtil.trim(part));
            }
        }
        if (StrUtil.isNotBlank(style)) {
            items.add("画风：" + StrUtil.trim(style));
        }
        items.add(usage);
        return String.join("，", items);
    }

    /**
     * 追加占位候选：上次失败留下的占位（地址为空）先去掉
     */
    @VisibleForTesting
    static List<AiDramaCharacterAppearanceDO.Candidate> appendPlaceholder(List<AiDramaCharacterAppearanceDO.Candidate> candidates,
                                                                         String prompt, Long modelId) {
        List<AiDramaCharacterAppearanceDO.Candidate> result = CollUtil.emptyIfNull(candidates).stream()
                .filter(candidate -> StrUtil.isNotEmpty(candidate.getUrl())).collect(Collectors.toCollection(ArrayList::new));
        result.add(new AiDramaCharacterAppearanceDO.Candidate().setDescription(prompt).setModelId(modelId));
        return result;
    }

    /**
     * 回填占位候选：成功时写入地址；失败时保留占位，用于重新提交时取模型
     */
    private static List<AiDramaCharacterAppearanceDO.Candidate> fillPlaceholder(List<AiDramaCharacterAppearanceDO.Candidate> candidates,
                                                                               String imageUrl) {
        List<AiDramaCharacterAppearanceDO.Candidate> result = new ArrayList<>(CollUtil.emptyIfNull(candidates));
        AiDramaCharacterAppearanceDO.Candidate last = CollUtil.getLast(result);
        if (imageUrl != null && last != null && StrUtil.isEmpty(last.getUrl())) {
            last.setUrl(imageUrl);
        }
        return result;
    }

    /**
     * 获得最后一个候选使用的绘画模型：用于重新提交
     */
    private static Long getLastModelId(List<AiDramaCharacterAppearanceDO.Candidate> candidates) {
        AiDramaCharacterAppearanceDO.Candidate last = CollUtil.getLast(CollUtil.emptyIfNull(candidates));
        return last != null ? last.getModelId() : null;
    }

    /**
     * 是否有成功的候选图
     */
    private static boolean hasImage(List<AiDramaCharacterAppearanceDO.Candidate> candidates) {
        return CollUtil.contains(CollUtil.emptyIfNull(candidates), candidate -> StrUtil.isNotEmpty(candidate.getUrl()));
    }

    /**
     * 校验选用的候选图：下标有效，且已生成成功；为空表示不修改
     */
    private static void validateSelectedIndex(List<AiDramaCharacterAppearanceDO.Candidate> candidates, Integer selectedIndex) {
        if (selectedIndex == null) {
            return;
        }
        if (candidates == null || selectedIndex < 0 || selectedIndex >= candidates.size()
                || StrUtil.isEmpty(candidates.get(selectedIndex).getUrl())) {
            throw exception(DRAMA_SELECTED_INDEX_INVALID);
        }
    }

    /**
     * 构建参考图列表：未设置时为空
     */
    private List<String> buildReferenceUrls(String referenceImageUrl) {
        return StrUtil.isNotBlank(referenceImageUrl) ? Collections.singletonList(referenceImageUrl) : Collections.emptyList();
    }

    /**
     * 获得第一个非空白的值
     */
    private static String firstNotBlank(String first, String second) {
        return StrUtil.isNotBlank(first) ? first : second;
    }

    /**
     * 校验不在生成中
     */
    private static void validateStatusNotRunning(Integer status) {
        if (AiDramaStatusEnum.isRunning(status)) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    /**
     * 校验生成中
     */
    private static void validateStatusRunning(Integer status) {
        if (!AiDramaStatusEnum.isRunning(status)) {
            throw exception(DRAMA_TASK_NOT_RUNNING);
        }
    }

    /**
     * 校验已失败
     */
    private static void validateStatusFail(Integer status) {
        if (ObjUtil.notEqual(status, AiDramaStatusEnum.FAIL.getStatus())) {
            throw exception(DRAMA_TASK_NOT_FAIL);
        }
    }

    // ========== 音频 ==========

    @Override
    public List<AiDramaAudioDO> getAudioList(Long projectId) {
        return audioMapper.selectListByProjectId(projectId);
    }

    @Override
    public AiDramaAudioDO getAudio(Long id) {
        return audioMapper.selectById(id);
    }

    @Override
    public Long createAudioMy(Long userId, AiDramaAudioCreateReqVO createReqVO) {
        // 1. 校验：样音必须关联本项目的角色；旁白不关联角色；选用样音通过 select 操作，不能直接上传
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);
        boolean sample = Objects.equals(createReqVO.getType(), AiDramaAudioTypeEnum.SAMPLE.getType());
        if (!sample && ObjUtil.notEqual(createReqVO.getType(), AiDramaAudioTypeEnum.NARRATION.getType())) {
            throw exception(DRAMA_AUDIO_TYPE_ERROR, createReqVO.getType());
        }
        Long characterId = sample ? validateCharacterOfProject(createReqVO.getCharacterId(), createReqVO.getProjectId()) : null;

        // 2. 插入
        AiDramaAudioDO audio = BeanUtils.toBean(createReqVO, AiDramaAudioDO.class, o -> o.setCharacterId(characterId));
        audioMapper.insert(audio);
        return audio.getId();
    }

    @Override
    public Long generateAudioMy(Long userId, AiDramaAudioGenerateReqVO reqVO) {
        // 1.1 校验项目、角色
        projectService.validateProjectMy(reqVO.getProjectId(), userId);
        Long characterId = reqVO.getCharacterId() != null
                ? validateCharacterOfProject(reqVO.getCharacterId(), reqVO.getProjectId()) : null;
        // 1.2 校验语音模型与其当前密钥，校验参数
        AiModelDO model = modelService.validateModel(reqVO.getModelId());
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.VOICE.getType())) {
            throw exception(MODEL_USE_TYPE_ERROR);
        }
        AiApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        if (ObjUtil.notEqual(model.getPlatform(), apiKey.getPlatform())) {
            throw exception(DRAMA_SPEECH_FAIL, StrUtil.format("模型的平台（{}）与 API 密钥的平台（{}）不一致",
                    model.getPlatform(), apiKey.getPlatform()));
        }
        AiSpeechModel speechModel;
        String format;
        try {
            speechModel = modelService.getTextToSpeechModel(apiKey, model.getModel());
            // 音频格式：取模型的默认格式，例如通义 Qwen3-TTS 只支持 wav；其它（腾讯云）为 mp3
            format = speechModel instanceof AbstractAiSpeechModel abstractModel
                    ? abstractModel.getDefaultFormat() : AiSpeechUtils.FORMAT_MP3;
            speechModel.validateOptions(reqVO.getText(), reqVO.getVoice(), format, reqVO.getSpeed());
        } catch (IllegalArgumentException e) {
            throw exception(DRAMA_SPEECH_FAIL, e.getMessage());
        }

        // 2. 同步合成：时限内未完成的（异步任务型平台）直接报错
        byte[] content;
        try {
            content = speechModel.synthesize(reqVO.getText(), reqVO.getVoice(), format, reqVO.getSpeed(), taskId -> {});
        } catch (Exception e) {
            log.error("[generateAudioMy][project({}) model({}) 合成失败]", reqVO.getProjectId(), model.getId(), e);
            throw exception(DRAMA_SPEECH_FAIL, StrUtil.blankToDefault(e.getMessage(), "调用语音模型失败"));
        }
        if (content == null) {
            throw exception(DRAMA_SPEECH_FAIL, "语音模型未在时限内返回音频，请换用其它语音模型");
        }

        // 3. 转存并保存
        // 文件名同文件服务默认的内容摘要，不同音频不共用同一地址；扩展名与合成的格式一致
        String fileUrl = fileApi.createFile(content, StrUtil.format("{}.{}",
                DigestUtil.sha256Hex(content), AiSpeechUtils.getFileExtension(format)));
        AiDramaAudioDO audio = new AiDramaAudioDO().setProjectId(reqVO.getProjectId()).setCharacterId(characterId)
                .setType(characterId != null ? AiDramaAudioTypeEnum.SAMPLE.getType() : AiDramaAudioTypeEnum.NARRATION.getType())
                .setFileUrl(fileUrl);
        audioMapper.insert(audio);
        return audio.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectAudioMy(Long userId, Long id) {
        // 1. 校验是角色样音
        AiDramaAudioDO audio = validateAudioMy(id, userId);
        if (audio.getCharacterId() == null || ObjUtil.equal(audio.getType(), AiDramaAudioTypeEnum.NARRATION.getType())) {
            throw exception(DRAMA_AUDIO_TYPE_ERROR, "只有角色样音可以选用");
        }

        // 2. 该角色原来选用的改回样音，再选用当前的
        audioMapper.updateByCharacterIdAndType(audio.getCharacterId(),
                Collections.singletonList(AiDramaAudioTypeEnum.SAMPLE_SELECTED.getType()),
                new AiDramaAudioDO().setType(AiDramaAudioTypeEnum.SAMPLE.getType()));
        audioMapper.updateById(new AiDramaAudioDO().setId(id).setType(AiDramaAudioTypeEnum.SAMPLE_SELECTED.getType()));
    }

    @Override
    public void deleteAudioMy(Long userId, Long id) {
        // 校验存在
        validateAudioMy(id, userId);
        // 删除
        audioMapper.deleteById(id);
    }

    /**
     * 校验音频存在，且属于该用户的项目
     */
    private AiDramaAudioDO validateAudioMy(Long id, Long userId) {
        AiDramaAudioDO audio = audioMapper.selectById(id);
        if (audio == null) {
            throw exception(DRAMA_AUDIO_NOT_EXISTS);
        }
        projectService.validateProjectMy(audio.getProjectId(), userId);
        return audio;
    }

    /**
     * 校验角色属于该项目
     *
     * @return 角色编号
     */
    private Long validateCharacterOfProject(Long characterId, Long projectId) {
        AiDramaCharacterDO character = characterId != null ? characterMapper.selectById(characterId) : null;
        if (character == null || ObjUtil.notEqual(character.getProjectId(), projectId)) {
            throw exception(DRAMA_CHARACTER_NOT_EXISTS);
        }
        return character.getId();
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     */
    private AiDramaAssetServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
