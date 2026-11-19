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
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.motion.AiDramaMotionReferenceCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import cn.iocoder.yudao.module.ai.service.music.AiMusicService;
import cn.iocoder.yudao.module.ai.util.AiFfmpegUtils;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.*;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAspectRatioEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStyleEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaTaskTypeEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaVisualAssetKindEnum;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillFileService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillService;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import javax.annotation.Resource;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
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
     * 通用绘画模型的出图尺寸
     */
    private static final int IMAGE_SIZE = 1024;
    /**
     * Seedream 的出图尺寸
     */
    private static final String SEEDREAM_SIZE = "2048x2048";
    /**
     * 技能用于出图、视频的说明的最大长度
     */
    private static final int SKILL_MEDIA_MAX_LENGTH = 2000;
    /**
     * 技能入口文件中，供出图、视频使用的段落：二级标题“媒体提示词”或“媒体执行脚本”
     */
    private static final Pattern SKILL_MEDIA_SECTION = Pattern.compile("(?ms)^##\\s+(?:媒体提示词|媒体执行脚本)\\s*$\\R(.*?)(?=^##\\s+|\\z)");
    /**
     * 技能入口文件开头的元数据
     */
    private static final Pattern SKILL_FRONTMATTER = Pattern.compile("(?s)\\A---\\R.*?\\R---\\R?");

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
    private AiDramaMotionReferenceMapper motionReferenceMapper;

    @Resource
    private AiDramaProjectService projectService;
    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;
    @Resource
    private AiSkillService skillService;
    @Resource
    private AiSkillFileService skillFileService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiDramaScriptService scriptService;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiMusicService musicService;

    @Resource
    private FileApi fileApi;

    @Value("${yudao.ai.drama.ffprobe-path:ffprobe}")
    private String ffprobePath;

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
                .setAppearanceIndex(0).setImageUrls(new ArrayList<>()).setImageDescriptions(new ArrayList<>()));
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

        // 2. 插入：序号排在已有形象之后（0 为主形象），候选图为空
        int maxIndex = appearanceMapper.selectListByCharacterId(createReqVO.getCharacterId()).stream()
                .mapToInt(AiDramaCharacterAppearanceDO::getAppearanceIndex).max().orElse(-1);
        AiDramaCharacterAppearanceDO appearance = BeanUtils.toBean(createReqVO, AiDramaCharacterAppearanceDO.class,
                o -> o.setId(null).setSelectedIndex(null).setAppearanceIndex(maxIndex + 1)
                        .setImageUrls(new ArrayList<>()).setImageDescriptions(new ArrayList<>()));
        appearanceMapper.insert(appearance);
        return appearance.getId();
    }

    @Override
    public void updateAppearanceMy(Long userId, AiDramaAppearanceSaveReqVO updateReqVO) {
        // 校验存在，且选用的候选图有效
        AiDramaCharacterAppearanceDO appearance = validateAppearanceMy(updateReqVO.getId(), userId);
        validateSelectedIndex(appearance.getImageUrls(), updateReqVO.getSelectedIndex());
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
    public void deleteAppearanceCandidateMy(Long userId, Long id, Integer index) {
        // 1. 校验存在、不在出图中，且候选图存在
        AiDramaCharacterAppearanceDO appearance = validateAppearanceMy(id, userId);
        validateStatusNotRunning(appearance.getStatus());
        validateImageIndex(appearance.getImageUrls(), index);

        // 2. 删除候选图，调整选用的下标；删除前的候选图记为上一版，用于撤销
        // 以读取到的状态、任务编号为条件，期间开始出图时不覆盖
        int updated = appearanceMapper.updateImagesByIdAndStatusAndTaskId(id, appearance.getStatus(), appearance.getTaskId(),
                new AiDramaCharacterAppearanceDO().setImageUrls(removeIndex(appearance.getImageUrls(), index))
                        .setImageDescriptions(removeIndex(appearance.getImageDescriptions(), index))
                        .setSelectedIndex(adjustSelectedIndex(appearance.getSelectedIndex(), index))
                        .setPreviousImageUrls(new ArrayList<>(appearance.getImageUrls()))
                        .setPreviousDescriptions(new ArrayList<>(CollUtil.emptyIfNull(appearance.getImageDescriptions()))));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    @Override
    public void undoAppearanceImageMy(Long userId, Long id) {
        // 1. 校验存在、不在出图中，且有上一版
        AiDramaCharacterAppearanceDO appearance = validateAppearanceMy(id, userId);
        validateStatusNotRunning(appearance.getStatus());
        if (appearance.getPreviousImageUrls() == null) {
            throw exception(DRAMA_VISUAL_ASSET_NO_PREVIOUS);
        }

        // 2. 恢复为上一版，清空上一版，选用最后一张；以读取到的状态、任务编号为条件，期间开始出图时不覆盖
        List<String> imageUrls = appearance.getPreviousImageUrls();
        int updated = appearanceMapper.updateImagesByIdAndStatusAndTaskId(id, appearance.getStatus(), appearance.getTaskId(),
                new AiDramaCharacterAppearanceDO().setImageUrls(imageUrls)
                        .setImageDescriptions(new ArrayList<>(CollUtil.emptyIfNull(appearance.getPreviousDescriptions())))
                        .setSelectedIndex(imageUrls.isEmpty() ? null : imageUrls.size() - 1));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
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
        submitAppearanceImage(appearance, buildImageClient(appearance.getModelId()));
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
     * 提交角色形象出图：记录模型、置为生成中，再异步出图；成功后追加为候选图
     */
    private void submitAppearanceImage(AiDramaCharacterAppearanceDO appearance, ImageClient client) {
        // 1. 校验不在生成中，拼接提示词
        validateStatusNotRunning(appearance.getStatus());
        AiDramaCharacterDO character = characterMapper.selectById(appearance.getCharacterId());
        AiDramaProjectDO project = projectService.validateProjectExists(character.getProjectId());
        String prompt = buildImagePrompt(buildStyleText(project), "人物设定图，单人，全身，纯色背景",
                character.getName(), firstNotBlank(character.getPrompt(), character.getDescription()),
                appearance.getDescription());
        // 2. 记录本次的模型、出图任务编号，置为生成中
        String taskId = IdUtil.fastSimpleUUID();
        appearanceMapper.updateById(new AiDramaCharacterAppearanceDO().setId(appearance.getId())
                .setModelId(client.model().getId()).setTaskId(taskId).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        // 3. 异步出图：通过代理调用，使 @Async 生效；形象参考图优先，其次角色参考图
        getSelf().executeImage(AiDramaTaskTypeEnum.APPEARANCE, appearance.getId(), taskId, client, prompt,
                buildReferenceUrls(firstNotBlank(appearance.getReferenceImageUrl(), character.getReferenceImageUrl())));
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
        return createLocation(BeanUtils.toBean(createReqVO, AiDramaLocationDO.class, location -> location.setId(null)
                .setSelectedIndex(null).setImageUrls(new ArrayList<>()).setImageDescriptions(new ArrayList<>())));
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
        validateSelectedIndex(location.getImageUrls(), updateReqVO.getSelectedIndex());
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
    public void deleteLocationCandidateMy(Long userId, Long id, Integer index) {
        // 1. 校验存在、不在出图中，且候选图存在
        AiDramaLocationDO location = validateLocationMy(id, userId);
        validateStatusNotRunning(location.getStatus());
        validateImageIndex(location.getImageUrls(), index);

        // 2. 删除候选图，调整选用的下标；删除前的候选图记为上一版，用于撤销
        // 以读取到的状态、任务编号为条件，期间开始出图时不覆盖
        int updated = locationMapper.updateImagesByIdAndStatusAndTaskId(id, location.getStatus(), location.getTaskId(),
                new AiDramaLocationDO().setImageUrls(removeIndex(location.getImageUrls(), index))
                        .setImageDescriptions(removeIndex(location.getImageDescriptions(), index))
                        .setSelectedIndex(adjustSelectedIndex(location.getSelectedIndex(), index))
                        .setPreviousImageUrls(new ArrayList<>(location.getImageUrls()))
                        .setPreviousDescriptions(new ArrayList<>(CollUtil.emptyIfNull(location.getImageDescriptions()))));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    @Override
    public void undoLocationImageMy(Long userId, Long id) {
        // 1. 校验存在、不在出图中，且有上一版
        AiDramaLocationDO location = validateLocationMy(id, userId);
        validateStatusNotRunning(location.getStatus());
        if (location.getPreviousImageUrls() == null) {
            throw exception(DRAMA_VISUAL_ASSET_NO_PREVIOUS);
        }

        // 2. 恢复为上一版，清空上一版，选用最后一张；以读取到的状态、任务编号为条件，期间开始出图时不覆盖
        List<String> imageUrls = location.getPreviousImageUrls();
        int updated = locationMapper.updateImagesByIdAndStatusAndTaskId(id, location.getStatus(), location.getTaskId(),
                new AiDramaLocationDO().setImageUrls(imageUrls)
                        .setImageDescriptions(new ArrayList<>(CollUtil.emptyIfNull(location.getPreviousDescriptions())))
                        .setSelectedIndex(imageUrls.isEmpty() ? null : imageUrls.size() - 1));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
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
        submitLocationImage(location, buildImageClient(location.getModelId()));
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
     * 提交场景出图：记录模型、置为生成中，再异步出图；成功后追加为候选图
     */
    private void submitLocationImage(AiDramaLocationDO location, ImageClient client) {
        // 1. 校验不在生成中，拼接提示词：有群众演员时描述群演，否则不出现人物
        validateStatusNotRunning(location.getStatus());
        AiDramaProjectDO project = projectService.validateProjectExists(location.getProjectId());
        String usage = Boolean.TRUE.equals(location.getCrowd())
                ? StrUtil.format("场景概念图，背景有群众演员{}", StrUtil.isNotBlank(location.getCrowdDescription())
                        ? "：" + location.getCrowdDescription() : "")
                : "场景概念图，无人物";
        String prompt = buildImagePrompt(buildStyleText(project), usage, location.getName(), location.getDescription(),
                CollUtil.join(CollUtil.emptyIfNull(location.getDescriptions()), "；"));
        // 2. 记录本次的模型、出图任务编号，置为生成中
        String taskId = IdUtil.fastSimpleUUID();
        locationMapper.updateById(new AiDramaLocationDO().setId(location.getId())
                .setModelId(client.model().getId()).setTaskId(taskId).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

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
    public List<AiDramaVisualAssetDO> getPropList(Long projectId) {
        return visualAssetMapper.selectListByProjectIdAndKindAndParentIdIsNull(projectId,
                AiDramaVisualAssetKindEnum.PROP.getType());
    }

    @Override
    public AiDramaVisualAssetDO getStoryboardFrame(Long storyboardId) {
        return visualAssetMapper.selectByStoryboardIdAndKindAndParentIdIsNull(storyboardId,
                AiDramaVisualAssetKindEnum.SHOT_FRAME.getType());
    }

    @Override
    public List<AiDramaVisualAssetDO> getStoryboardFrameList(Collection<Long> storyboardIds) {
        if (CollUtil.isEmpty(storyboardIds)) {
            return Collections.emptyList();
        }
        return visualAssetMapper.selectListByStoryboardIdsAndKindAndParentIdIsNull(storyboardIds,
                AiDramaVisualAssetKindEnum.SHOT_FRAME.getType());
    }

    @Override
    public Long createVisualAssetMy(Long userId, AiDramaVisualAssetSaveReqVO createReqVO) {
        // 校验项目
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);
        // 插入道具：参考图、出现的镜头序号记在参考图中
        return createVisualAsset(new AiDramaVisualAssetDO().setProjectId(createReqVO.getProjectId())
                .setKind(AiDramaVisualAssetKindEnum.PROP.getType()).setTitle(createReqVO.getTitle())
                .setPrompt(createReqVO.getPrompt()).setReferenceImages(new AiDramaVisualAssetDO.ReferenceImages()
                        .setReferenceImageUrl(createReqVO.getReferenceImageUrl()).setShotNumbers(createReqVO.getShotNumbers())));
    }

    @Override
    public Long createVisualAsset(AiDramaVisualAssetDO visualAsset) {
        visualAssetMapper.insert(visualAsset);
        return visualAsset.getId();
    }

    @Override
    public void updateVisualAssetMy(Long userId, AiDramaVisualAssetSaveReqVO updateReqVO) {
        // 1. 校验是当前的道具
        AiDramaVisualAssetDO visualAsset = validateVisualAssetMy(updateReqVO.getId(), userId);
        validateCurrentProp(visualAsset);

        // 2. 更新：参考图中的其它字段保留
        AiDramaVisualAssetDO.ReferenceImages referenceImages = ObjUtil.defaultIfNull(visualAsset.getReferenceImages(),
                AiDramaVisualAssetDO.ReferenceImages::new);
        referenceImages.setReferenceImageUrl(updateReqVO.getReferenceImageUrl()).setShotNumbers(updateReqVO.getShotNumbers());
        visualAssetMapper.updateById(new AiDramaVisualAssetDO().setId(visualAsset.getId()).setTitle(updateReqVO.getTitle())
                .setPrompt(updateReqVO.getPrompt()).setReferenceImages(referenceImages));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVisualAssetMy(Long userId, Long id) {
        // 校验是当前的道具
        AiDramaVisualAssetDO visualAsset = validateVisualAssetMy(id, userId);
        validateCurrentProp(visualAsset);
        // 删除：历史版本一并删除
        visualAssetMapper.deleteByParentId(id);
        visualAssetMapper.deleteById(id);
    }

    @Override
    public void generateVisualAssetImageMy(Long userId, AiDramaImageGenerateReqVO reqVO) {
        // 校验是当前的道具
        AiDramaVisualAssetDO visualAsset = validateVisualAssetMy(reqVO.getId(), userId);
        validateCurrentProp(visualAsset);
        // 提交出图
        submitVisualAssetImage(visualAsset, buildImageClient(reqVO.getModelId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void undoVisualAssetImageMy(Long userId, Long id) {
        // 1. 校验是当前版本（道具、关键帧）、不在出图中，且有历史版本
        AiDramaVisualAssetDO current = validateVisualAssetMy(id, userId);
        if (current.getParentId() != null) {
            throw exception(DRAMA_VISUAL_ASSET_NOT_EXISTS);
        }
        validateStatusNotRunning(current.getStatus());
        AiDramaVisualAssetDO archive = visualAssetMapper.selectLatestByParentId(id);
        if (archive == null) {
            throw exception(DRAMA_VISUAL_ASSET_NO_PREVIOUS);
        }

        // 2. 当前版本与最近的历史版本互换：以读取到的状态、任务编号为条件，期间开始出图时不覆盖
        int updated = visualAssetMapper.updateVersionByIdAndStatusAndTaskId(id, current.getStatus(), current.getTaskId(),
                copyVersion(archive, new AiDramaVisualAssetDO()));
        if (updated == 0) {
            throw exception(DRAMA_TASK_RUNNING);
        }
        visualAssetMapper.updateVersionByIdAndStatusAndTaskId(archive.getId(), archive.getStatus(), archive.getTaskId(),
                copyVersion(current, new AiDramaVisualAssetDO()));
    }

    /**
     * 复制视觉资产的版本字段：归档、撤销时使用
     */
    private static AiDramaVisualAssetDO copyVersion(AiDramaVisualAssetDO source, AiDramaVisualAssetDO target) {
        return target.setStoryboardId(source.getStoryboardId()).setTitle(source.getTitle()).setPrompt(source.getPrompt())
                .setReferenceImages(source.getReferenceImages()).setSourceHash(source.getSourceHash())
                .setImageUrl(source.getImageUrl()).setModelId(source.getModelId()).setModel(source.getModel())
                .setTaskId(source.getTaskId()).setStatus(source.getStatus()).setErrorMessage(source.getErrorMessage());
    }

    @Override
    public void archiveVisualAsset(AiDramaVisualAssetDO current) {
        if (StrUtil.isEmpty(current.getImageUrl())) {
            return;
        }
        Integer kind = ObjUtil.equal(current.getKind(), AiDramaVisualAssetKindEnum.SHOT_FRAME.getType())
                ? AiDramaVisualAssetKindEnum.ARCHIVED_SHOT_FRAME.getType() : AiDramaVisualAssetKindEnum.ARCHIVED_PROP.getType();
        visualAssetMapper.insert(copyVersion(current, new AiDramaVisualAssetDO()).setProjectId(current.getProjectId())
                .setParentId(current.getId()).setKind(kind));
    }

    @Override
    public int updateVisualAssetByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId,
                                                       AiDramaVisualAssetDO updateObj) {
        return visualAssetMapper.updateByIdAndStatusAndTaskId(id, whereStatus, whereTaskId, updateObj);
    }

    /**
     * 校验是当前的道具：历史版本、关键帧不能直接修改
     */
    private static void validateCurrentProp(AiDramaVisualAssetDO visualAsset) {
        if (visualAsset.getParentId() != null
                || ObjUtil.notEqual(visualAsset.getKind(), AiDramaVisualAssetKindEnum.PROP.getType())) {
            throw exception(DRAMA_VISUAL_ASSET_NOT_EXISTS);
        }
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
     * 提交视觉资产出图：已有图片时先归档为历史版本，再记录模型、置为生成中，异步出图
     *
     * 关键帧（重新提交时）沿用记录的完整提示词与参考图；道具按设定拼接提示词，使用道具参考图
     */
    private void submitVisualAssetImage(AiDramaVisualAssetDO visualAsset, ImageClient client) {
        // 1. 校验不在生成中，拼接提示词
        validateStatusNotRunning(visualAsset.getStatus());
        AiDramaProjectDO project = projectService.validateProjectExists(visualAsset.getProjectId());
        AiDramaVisualAssetDO.ReferenceImages referenceImages = visualAsset.getReferenceImages();
        boolean frame = ObjUtil.equal(visualAsset.getKind(), AiDramaVisualAssetKindEnum.SHOT_FRAME.getType());
        String prompt = frame ? visualAsset.getPrompt() : buildImagePrompt(buildStyleText(project), "道具设定图，纯色背景",
                visualAsset.getTitle(), visualAsset.getPrompt());
        List<String> referenceUrls = frame ? (referenceImages != null ? CollUtil.emptyIfNull(referenceImages.getUrls())
                : Collections.emptyList()) : buildReferenceUrls(referenceImages != null ? referenceImages.getReferenceImageUrl() : null);
        // 2. 归档当前版本，置为生成中
        archiveVisualAsset(visualAsset);
        String taskId = IdUtil.fastSimpleUUID();
        visualAssetMapper.updateById(new AiDramaVisualAssetDO().setId(visualAsset.getId())
                .setModelId(client.model().getId()).setModel(client.model().getModel())
                .setTaskId(taskId).setStatus(AiDramaStatusEnum.IN_PROGRESS.getStatus()));

        // 3. 异步出图
        getSelf().executeImage(AiDramaTaskTypeEnum.VISUAL_ASSET, visualAsset.getId(), taskId, client, prompt, referenceUrls);
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
            if (!AiDramaStatusEnum.isRunning(appearance.getStatus()) && CollUtil.isEmpty(appearance.getImageUrls())) {
                submitAppearanceImage(appearance, client);
                count++;
            }
        }
        // 2.2 场景：没有任何成功的候选
        for (AiDramaLocationDO location : locationMapper.selectListByProjectId(project.getId())) {
            if (!AiDramaStatusEnum.isRunning(location.getStatus()) && CollUtil.isEmpty(location.getImageUrls())) {
                submitLocationImage(location, client);
                count++;
            }
        }
        // 2.3 道具：没有图片；关键帧在分镜中生成
        for (AiDramaVisualAssetDO visualAsset : getPropList(project.getId())) {
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
        motionReferenceMapper.deleteByProjectId(projectId);
    }

    // ========== 出图 ==========

    /**
     * 绘画客户端：Seedream 使用火山方舟，其它使用 Spring AI 的 ImageModel
     */
    @lombok.Value
    @Accessors(fluent = true)
    public static class ImageClient {

        AiModelDO model;
        ArkService arkService;
        ImageModel imageModel;

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
            imageUrl = drawImage(client, prompt, referenceUrls, SEEDREAM_SIZE);
        } catch (Exception e) {
            log.error("[executeImage][type({}) id({}) 出图失败]", type.getType(), id, e);
            errorMessage = StrUtil.blankToDefault(e.getMessage(), "出图失败");
        }
        // 2. 回写结果
        Integer status = imageUrl != null ? AiDramaStatusEnum.SUCCESS.getStatus() : AiDramaStatusEnum.FAIL.getStatus();
        Integer whereStatus = AiDramaStatusEnum.IN_PROGRESS.getStatus();
        switch (type) {
            case APPEARANCE: {
                // 成功时追加候选图，原来的候选图记为上一版，用于撤销；被重新提交时任务编号已变，下面的条件更新不会写入
                AiDramaCharacterAppearanceDO appearance = appearanceMapper.selectById(id);
                if (appearance == null) {
                    return;
                }
                AiDramaCharacterAppearanceDO updateObj = new AiDramaCharacterAppearanceDO().setStatus(status).setErrorMessage(errorMessage);
                if (imageUrl != null) {
                    List<String> imageUrls = new ArrayList<>(CollUtil.emptyIfNull(appearance.getImageUrls()));
                    updateObj.setPreviousImageUrls(new ArrayList<>(imageUrls))
                            .setPreviousDescriptions(new ArrayList<>(CollUtil.emptyIfNull(appearance.getImageDescriptions())))
                            .setImageUrls(append(imageUrls, imageUrl))
                            .setImageDescriptions(append(appearance.getImageDescriptions(), prompt))
                            .setSelectedIndex(appearance.getSelectedIndex() == null ? imageUrls.size() : null);
                }
                appearanceMapper.updateByIdAndStatusAndTaskId(id, whereStatus, taskId, updateObj);
                break;
            }
            case LOCATION: {
                AiDramaLocationDO location = locationMapper.selectById(id);
                if (location == null) {
                    return;
                }
                AiDramaLocationDO updateObj = new AiDramaLocationDO().setStatus(status).setErrorMessage(errorMessage);
                if (imageUrl != null) {
                    List<String> imageUrls = new ArrayList<>(CollUtil.emptyIfNull(location.getImageUrls()));
                    updateObj.setPreviousImageUrls(new ArrayList<>(imageUrls))
                            .setPreviousDescriptions(new ArrayList<>(CollUtil.emptyIfNull(location.getImageDescriptions())))
                            .setImageUrls(append(imageUrls, imageUrl))
                            .setImageDescriptions(append(location.getImageDescriptions(), prompt))
                            .setSelectedIndex(location.getSelectedIndex() == null ? imageUrls.size() : null);
                }
                locationMapper.updateByIdAndStatusAndTaskId(id, whereStatus, taskId, updateObj);
                break;
            }
            case VISUAL_ASSET: {
                // 原来的图片已在提交时归档为历史版本
                visualAssetMapper.updateByIdAndStatusAndTaskId(id, whereStatus, taskId, new AiDramaVisualAssetDO()
                        .setImageUrl(imageUrl).setStatus(status).setErrorMessage(errorMessage));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void validateImageModel(Long modelId) {
        buildImageClient(modelId);
    }

    @Override
    public void drawImageAsync(Long modelId, String prompt, List<String> referenceUrls, String aspectRatio,
                               BiConsumer<String, String> callback) {
        // 按画幅取 Seedream 出图尺寸：画幅为空、不在枚举中时使用默认尺寸
        AiDramaAspectRatioEnum ratio = AiDramaAspectRatioEnum.valueOfRatio(aspectRatio);
        // 先同步创建客户端：模型、密钥不可用时直接报错；再通过代理异步出图，使 @Async 生效
        getSelf().executeDrawImage(buildImageClient(modelId), prompt, referenceUrls,
                ratio != null ? ratio.getSeedreamSize() : SEEDREAM_SIZE, callback);
    }

    @Override
    public void generateStoryboardFrame(AiDramaStoryboardDO storyboard, Long modelId, String prompt,
                                        AiDramaVisualAssetDO.ReferenceImages referenceImages, String sourceHash,
                                        String aspectRatio) {
        // 1.1 校验绘画模型
        ImageClient client = buildImageClient(modelId);
        // 1.2 当前关键帧：没有时新建；有时校验不在出图中，并归档已有的图片；镜头未填标题时，按序号命名
        String title = StrUtil.blankToDefault(storyboard.getTitle(), "镜头 " + storyboard.getSceneNo());
        AiDramaVisualAssetDO frame = getStoryboardFrame(storyboard.getId());
        if (frame == null) {
            frame = new AiDramaVisualAssetDO().setProjectId(storyboard.getProjectId()).setStoryboardId(storyboard.getId())
                    .setKind(AiDramaVisualAssetKindEnum.SHOT_FRAME.getType()).setTitle(title);
            visualAssetMapper.insert(frame);
        } else {
            validateStatusNotRunning(frame.getStatus());
            archiveVisualAsset(frame);
        }

        // 2. 记录提示词、参考图，置为生成中；输入摘要用于判断关键帧是否对应当前的输入
        List<String> referenceUrls = CollUtil.emptyIfNull(referenceImages.getUrls());
        String taskId = IdUtil.fastSimpleUUID();
        Integer whereStatus = AiDramaStatusEnum.IN_PROGRESS.getStatus();
        visualAssetMapper.updateById(new AiDramaVisualAssetDO().setId(frame.getId()).setTitle(title)
                .setPrompt(prompt).setReferenceImages(referenceImages).setSourceHash(sourceHash)
                .setModelId(client.model().getId()).setModel(client.model().getModel())
                .setTaskId(taskId).setStatus(whereStatus));

        // 3. 按画幅异步出图：结果以“仍为生成中、且仍是本次的任务编号”为条件回写
        Long id = frame.getId();
        AiDramaAspectRatioEnum ratio = AiDramaAspectRatioEnum.valueOfRatio(aspectRatio);
        getSelf().executeDrawImage(client, prompt, referenceUrls, ratio != null ? ratio.getSeedreamSize() : SEEDREAM_SIZE,
                (imageUrl, errorMessage) -> visualAssetMapper.updateByIdAndStatusAndTaskId(id, whereStatus, taskId,
                        new AiDramaVisualAssetDO().setImageUrl(imageUrl).setErrorMessage(errorMessage).setStatus(imageUrl != null
                                ? AiDramaStatusEnum.SUCCESS.getStatus() : AiDramaStatusEnum.FAIL.getStatus())));
    }

    @Override
    public void deleteStoryboardFrameList(Collection<Long> storyboardIds) {
        if (CollUtil.isEmpty(storyboardIds)) {
            return;
        }
        visualAssetMapper.deleteByStoryboardIds(storyboardIds);
    }

    /**
     * 异步出图：成功、失败都回调
     */
    @Async
    public void executeDrawImage(ImageClient client, String prompt, List<String> referenceUrls, String seedreamSize,
                                 BiConsumer<String, String> callback) {
        String imageUrl;
        try {
            imageUrl = drawImage(client, prompt, referenceUrls, seedreamSize);
        } catch (Exception e) {
            log.error("[executeDrawImage][model({}) 出图失败]", client.model().getId(), e);
            callback.accept(null, StrUtil.blankToDefault(e.getMessage(), "出图失败"));
            return;
        }
        callback.accept(imageUrl, null);
    }

    /**
     * 调用绘画模型出图，转存到文件服务
     *
     * @param seedreamSize Seedream 的尺寸，例如 1440x2560；其它平台固定为正方形
     * @return 文件地址
     */
    private String drawImage(ImageClient client, String prompt, List<String> referenceUrls, String seedreamSize) {
        AiModelDO model = client.model();
        // 1. Seedream：返回临时地址，参考图需要可公开访问
        if (client.arkService() != null) {
            AiImageDO image = new AiImageDO().setModel(model.getModel()).setPrompt(prompt)
                    .setSeedreamOptions(new AiImageDO.SeedreamOptions().setSize(seedreamSize))
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
     * 拼接出图的画风：画风枚举的名称，随剧本基调时取剧本的叙事基调；剧本有世界观时追加；绑定了美学技能时追加技能的媒体说明
     */
    @Override
    public String buildStyleText(AiDramaProjectDO project) {
        List<String> items = new ArrayList<>();
        AiDramaStyleEnum style = ArrayUtil.firstMatch(item -> item.getStyle().equals(project.getStyle()), AiDramaStyleEnum.values());
        AiDramaScriptDO script = scriptService.getScriptByProjectId(project.getId());
        if (style == AiDramaStyleEnum.SCRIPT_TONE) {
            if (script != null && StrUtil.isNotBlank(script.getTone())) {
                items.add(script.getTone());
            }
        } else if (style != null) {
            items.add(style.getName());
        }
        if (script != null && StrUtil.isNotBlank(script.getWorldbuilding())) {
            items.add("世界观：" + StrUtil.trim(script.getWorldbuilding()));
        }
        if (project.getAestheticSkillId() != null) {
            AiSkillDO skill = skillService.getSkill(project.getAestheticSkillId());
            String skillText = skill != null ? buildSkillMediaText(skill) : null;
            if (StrUtil.isNotBlank(skillText)) {
                items.add(skillText);
            }
        }
        return String.join("，", items);
    }

    /**
     * 获得技能用于出图、视频的说明：取入口文件 SKILL.md 中的“媒体提示词”段；没有该段时，正文不超过上限取正文，否则取技能描述
     *
     * 完整正文用于剧本、分镜等对话阶段；媒体请求只带精简的执行说明，避免长篇手册挤占提示词
     */
    private String buildSkillMediaText(AiSkillDO skill) {
        // 1. 入口文件的正文：去掉开头的元数据
        AiSkillFileDO file = CollUtil.findOne(skillFileService.getSkillFileListBySkillId(skill.getId()), item ->
                Objects.equals(item.getParentId(), AiSkillFileDO.PARENT_ID_ROOT) && AiSkillFileDO.NAME_SKILL.equals(item.getName()));
        String body = file != null ? StrUtil.trim(SKILL_FRONTMATTER.matcher(StrUtil.nullToEmpty(file.getContent())).replaceFirst(""))
                : "";
        // 2.1 有“媒体提示词”段时使用该段：超过上限时报错，由用户精简
        Matcher section = SKILL_MEDIA_SECTION.matcher(body);
        if (section.find()) {
            String text = StrUtil.trim(section.group(1));
            if (text.length() > SKILL_MEDIA_MAX_LENGTH) {
                throw invalidParamException("技能「{}」的媒体提示词段最多 {} 个字符，请精简后再生成", skill.getName(),
                        SKILL_MEDIA_MAX_LENGTH);
            }
            return text;
        }
        // 2.2 没有时：正文不超过上限取正文，否则取技能描述
        return StrUtil.isNotBlank(body) && body.length() <= SKILL_MEDIA_MAX_LENGTH ? body : StrUtil.trim(skill.getDescription());
    }

    /**
     * 追加一项：返回新的列表，不修改原列表
     */
    private static List<String> append(List<String> list, String item) {
        List<String> result = new ArrayList<>(CollUtil.emptyIfNull(list));
        result.add(item);
        return result;
    }

    /**
     * 删除一项：返回新的列表；下标超出时原样返回
     */
    private static List<String> removeIndex(List<String> list, int index) {
        List<String> result = new ArrayList<>(CollUtil.emptyIfNull(list));
        if (index < result.size()) {
            result.remove(index);
        }
        return result;
    }

    /**
     * 校验候选图下标存在
     */
    private static void validateImageIndex(List<String> imageUrls, Integer index) {
        if (imageUrls == null || index == null || index < 0 || index >= imageUrls.size()) {
            throw exception(DRAMA_SELECTED_INDEX_INVALID);
        }
    }

    /**
     * 校验选用的候选图：为空表示不修改
     */
    private static void validateSelectedIndex(List<String> imageUrls, Integer selectedIndex) {
        if (selectedIndex != null) {
            validateImageIndex(imageUrls, selectedIndex);
        }
    }

    /**
     * 删除候选图后的选用下标：删除的正是选用的，改为未选用；删除的在选用之前，下标前移
     */
    @VisibleForTesting
    static Integer adjustSelectedIndex(Integer selectedIndex, int removedIndex) {
        if (selectedIndex == null || selectedIndex == removedIndex) {
            return null;
        }
        return selectedIndex > removedIndex ? selectedIndex - 1 : selectedIndex;
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
        // 1. 校验：样音必须关联本项目的角色，其它不关联角色；对白随镜头生成，不能直接上传
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);
        if (Objects.equals(createReqVO.getType(), AiDramaAudioTypeEnum.DIALOGUE.getType())) {
            throw exception(DRAMA_AUDIO_TYPE_ERROR, createReqVO.getType());
        }
        boolean sample = Objects.equals(createReqVO.getType(), AiDramaAudioTypeEnum.SAMPLE.getType());
        Long characterId = sample ? validateCharacterOfProject(createReqVO.getCharacterId(), createReqVO.getProjectId()) : null;
        // 使用的分镜只有配乐、参考音频有
        boolean placement = isPlacementAudio(createReqVO.getType());

        // 2. 插入
        AiDramaAudioDO audio = BeanUtils.toBean(createReqVO, AiDramaAudioDO.class, o -> o.setCharacterId(characterId)
                .setStoryboardIds(placement ? createReqVO.getStoryboardIds() : null));
        audioMapper.insert(audio);
        return audio.getId();
    }

    @Override
    public void updateAudioMy(Long userId, AiDramaAudioUpdateReqVO updateReqVO) {
        // 1. 校验是配乐、参考音频
        AiDramaAudioDO audio = validateAudioMy(updateReqVO.getId(), userId);
        if (!isPlacementAudio(audio.getType())) {
            throw exception(DRAMA_AUDIO_TYPE_ERROR, audio.getType());
        }
        // 2. 更新：使用的分镜为空时清空，表示候选
        audioMapper.updateForSave(new AiDramaAudioDO().setId(audio.getId()).setName(updateReqVO.getName())
                .setStoryboardIds(updateReqVO.getStoryboardIds()).setVolume(updateReqVO.getVolume())
                .setStartOffset(updateReqVO.getStartOffset()));
    }

    @Override
    public List<AiDramaAudioDO> getReferenceAudioList(Long projectId) {
        return audioMapper.selectListByProjectIdAndType(projectId, AiDramaAudioTypeEnum.REFERENCE.getType());
    }

    /**
     * 是否是按分镜使用的音频：配乐、参考音频
     */
    private static boolean isPlacementAudio(Integer type) {
        return Objects.equals(type, AiDramaAudioTypeEnum.BGM.getType())
                || Objects.equals(type, AiDramaAudioTypeEnum.REFERENCE.getType());
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
            format = speechModel instanceof AbstractAiSpeechModel
                    ? ((AbstractAiSpeechModel) speechModel).getDefaultFormat() : AiSpeechUtils.FORMAT_MP3;
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
                .setText(reqVO.getText()).setVoice(reqVO.getVoice()).setFileUrl(fileUrl);
        audioMapper.insert(audio);
        return audio.getId();
    }

    @Override
    public void selectAudioMy(Long userId, Long id) {
        // 1. 校验是角色样音
        AiDramaAudioDO audio = validateAudioMy(id, userId);
        if (audio.getCharacterId() == null || ObjUtil.notEqual(audio.getType(), AiDramaAudioTypeEnum.SAMPLE.getType())) {
            throw exception(DRAMA_AUDIO_TYPE_ERROR, "只有角色样音可以选用");
        }
        // 2. 记录为角色选用的样音
        characterMapper.updateById(new AiDramaCharacterDO().setId(audio.getCharacterId()).setSampleAudioId(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAudioMy(Long userId, Long id) {
        // 1. 校验存在
        AiDramaAudioDO audio = validateAudioMy(id, userId);
        // 2. 删除；是角色选用的样音时，取消选用
        audioMapper.deleteById(id);
        if (audio.getCharacterId() != null) {
            characterMapper.update(null, new LambdaUpdateWrapper<AiDramaCharacterDO>()
                    .eq(AiDramaCharacterDO::getId, audio.getCharacterId())
                    .eq(AiDramaCharacterDO::getSampleAudioId, id)
                    .set(AiDramaCharacterDO::getSampleAudioId, null));
        }
    }

    @Override
    public Long createMusicAudioMy(Long userId, AiDramaAudioMusicReqVO reqVO) {
        // 1. 校验项目；AI 音乐是我的、已生成完成
        projectService.validateProjectMy(reqVO.getProjectId(), userId);
        AiMusicDO music = musicService.getMusic(reqVO.getMusicId());
        if (music == null || ObjUtil.notEqual(music.getUserId(), userId)
                || ObjUtil.notEqual(music.getStatus(), AiMusicStatusEnum.SUCCESS.getStatus()) || StrUtil.isEmpty(music.getAudioUrl())) {
            throw exception(DRAMA_MUSIC_NOT_READY);
        }

        // 2. 插入配乐：复制音乐的文件地址、时长
        AiDramaAudioDO audio = new AiDramaAudioDO().setProjectId(reqVO.getProjectId())
                .setType(AiDramaAudioTypeEnum.BGM.getType()).setName(StrUtil.blankToDefault(music.getTitle(), "AI 配乐"))
                .setText(music.getLyric()).setFileUrl(music.getAudioUrl()).setDuration(music.getDuration())
                .setStoryboardIds(reqVO.getStoryboardIds()).setVolume(AiFfmpegUtils.DEFAULT_MUSIC_VOLUME)
                .setStartOffset(0D).setMusicId(music.getId());
        audioMapper.insert(audio);
        return audio.getId();
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

    // ========== 动作参考 ==========

    @Override
    public List<AiDramaMotionReferenceDO> getMotionReferenceList(Long projectId) {
        return motionReferenceMapper.selectListByProjectId(projectId);
    }

    @Override
    public Long createMotionReferenceMy(Long userId, AiDramaMotionReferenceCreateReqVO createReqVO) {
        // 1.1 校验项目
        projectService.validateProjectMy(createReqVO.getProjectId(), userId);
        // 1.2 读取视频信息，校验符合动作参考的要求
        AiFfmpegUtils.VideoInfo info;
        try {
            info = AiFfmpegUtils.probeVideo(ffprobePath, fileApi.presignGetUrlQuietly(createReqVO.getFileUrl()));
        } catch (Exception e) {
            log.warn("[createMotionReferenceMy][project({}) url({}) 读取视频失败]", createReqVO.getProjectId(),
                    createReqVO.getFileUrl(), e);
            throw exception(DRAMA_MOTION_REFERENCE_INVALID, "无法读取视频");
        }
        validateMotionVideo(info);

        // 2. 插入
        AiDramaMotionReferenceDO reference = new AiDramaMotionReferenceDO().setProjectId(createReqVO.getProjectId())
                .setName(StrUtil.trim(createReqVO.getName())).setFileUrl(createReqVO.getFileUrl()).setDuration(info.getDuration());
        motionReferenceMapper.insert(reference);
        return reference.getId();
    }

    /**
     * 校验动作参考视频：2 ~ 30 秒、24 ~ 60fps 的 H.264 / H.265，宽高比 0.4 ~ 2.5，不低于 480p
     */
    @VisibleForTesting
    static void validateMotionVideo(AiFfmpegUtils.VideoInfo info) {
        double aspect = info.getHeight() > 0 ? (double) info.getWidth() / info.getHeight() : 0;
        long pixels = (long) info.getWidth() * info.getHeight();
        if (!ArrayUtil.contains(new String[]{"h264", "hevc"}, info.getCodec())
                || info.getDuration() < 2 || info.getDuration() > 30.05 || info.getFps() < 23.9 || info.getFps() > 60.1
                || info.getWidth() < 300 || info.getHeight() < 300 || info.getWidth() > 6000 || info.getHeight() > 6000
                || aspect < 0.4 || aspect > 2.5 || pixels < 409600 || pixels > 8295044) {
            throw exception(DRAMA_MOTION_REFERENCE_INVALID,
                    "须为 2 ~ 30 秒、24 ~ 60fps 的 H.264 / H.265 视频，宽高比 0.4 ~ 2.5，且不低于 480p");
        }
    }

    @Override
    public void deleteMotionReferenceMy(Long userId, Long id) {
        // 校验存在
        AiDramaMotionReferenceDO reference = motionReferenceMapper.selectById(id);
        if (reference == null) {
            throw exception(DRAMA_MOTION_REFERENCE_NOT_EXISTS);
        }
        projectService.validateProjectMy(reference.getProjectId(), userId);
        // 删除
        motionReferenceMapper.deleteById(id);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     */
    private AiDramaAssetServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
