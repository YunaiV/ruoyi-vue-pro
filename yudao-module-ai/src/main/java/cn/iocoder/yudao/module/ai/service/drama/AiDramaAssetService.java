package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

// DONE @AI：为什么“同步校验模型后置为生成中，异步调用绘画模型并转存；Midjourney 不支持”不支持噢？（Midjourney 没有同步出图的 ImageModel：要通过 MidjourneyApi 提交后，由 AiMidjourneySyncJob 轮询结果；短剧出图是一次调用直接拿到图片，所以不支持。说明移到实现类的 buildImageClient 上）
/**
 * AI 短剧资产 Service 接口
 *
 * @author 芋道源码
 */
public interface AiDramaAssetService {

    // ========== 角色 ==========

    /**
     * 获得项目的角色列表
     *
     * @param projectId 项目编号
     * @return 角色列表
     */
    List<AiDramaCharacterDO> getCharacterList(Long projectId);

    /**
     * 获得角色列表
     *
     * @param ids 编号
     * @return 角色列表
     */
    List<AiDramaCharacterDO> getCharacterList(Collection<Long> ids);

    /**
     * 创建我的角色：同时创建第 1 个形象
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCharacterMy(Long userId, AiDramaCharacterSaveReqVO createReqVO);

    /**
     * 创建角色：同时创建第 1 个形象；不校验项目归属，用于从剧本分析资产
     *
     * @param character 角色
     * @return 编号
     */
    Long createCharacter(AiDramaCharacterDO character);

    /**
     * 修改我的角色
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateCharacterMy(Long userId, AiDramaCharacterSaveReqVO updateReqVO);

    /**
     * 删除我的角色：一并删除形象、样音
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteCharacterMy(Long userId, Long id);

    // ========== 角色形象 ==========

    /**
     * 获得角色的形象列表
     *
     * @param characterIds 角色编号
     * @return 角色形象列表
     */
    List<AiDramaCharacterAppearanceDO> getAppearanceListByCharacterIds(Collection<Long> characterIds);

    /**
     * 获得角色的形象 Map
     *
     * @param characterIds 角色编号
     * @return 角色形象 Map：key 为角色编号
     */
    default Map<Long, List<AiDramaCharacterAppearanceDO>> getAppearanceMapByCharacterIds(Collection<Long> characterIds) {
        return convertMultiMap(getAppearanceListByCharacterIds(characterIds), AiDramaCharacterAppearanceDO::getCharacterId);
    }

    /**
     * 获得角色形象
     *
     * @param id 编号
     * @return 角色形象
     */
    AiDramaCharacterAppearanceDO getAppearance(Long id);

    /**
     * 获得角色形象列表
     *
     * @param ids 编号
     * @return 角色形象列表
     */
    List<AiDramaCharacterAppearanceDO> getAppearanceList(Collection<Long> ids);

    /**
     * 创建我的角色形象：序号排在该角色已有形象之后
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAppearanceMy(Long userId, AiDramaAppearanceSaveReqVO createReqVO);

    /**
     * 修改我的角色形象：选用的候选图需已生成成功
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateAppearanceMy(Long userId, AiDramaAppearanceSaveReqVO updateReqVO);

    /**
     * 删除我的角色形象
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteAppearanceMy(Long userId, Long id);

    /**
     * 生成我的角色形象的候选图：同步校验模型后置为生成中，异步调用绘画模型并转存
     *
     * @param userId 用户编号
     * @param reqVO 请求：id 为角色形象编号
     */
    void generateAppearanceImageMy(Long userId, AiDramaImageGenerateReqVO reqVO);

    /**
     * 重新提交已失败的角色形象：使用上次的绘画模型
     *
     * @param id 编号
     */
    void resubmitAppearanceImage(Long id);

    /**
     * 获得角色形象的生成任务分页：生成过的角色形象
     *
     * @param pageParam 分页参数
     * @param reqVO 筛选条件
     * @param projectIds 项目编号；为空表示不过滤
     * @return 分页
     */
    PageResult<AiDramaCharacterAppearanceDO> getAppearanceTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                                   Collection<Long> projectIds);

    /**
     * 把生成中的角色形象标记为已失败
     *
     * @param id 编号
     * @param errorMessage 失败原因
     */
    void updateAppearanceFail(Long id, String errorMessage);

    // ========== 场景 ==========

    /**
     * 获得项目的场景列表
     *
     * @param projectId 项目编号
     * @return 场景列表
     */
    List<AiDramaLocationDO> getLocationList(Long projectId);

    /**
     * 获得场景
     *
     * @param id 编号
     * @return 场景
     */
    AiDramaLocationDO getLocation(Long id);

    /**
     * 创建我的场景
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createLocationMy(Long userId, AiDramaLocationSaveReqVO createReqVO);

    /**
     * 创建场景：不校验项目归属，用于从剧本分析资产
     *
     * @param location 场景
     * @return 编号
     */
    Long createLocation(AiDramaLocationDO location);

    /**
     * 修改我的场景：选用的候选图需已生成成功
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateLocationMy(Long userId, AiDramaLocationSaveReqVO updateReqVO);

    /**
     * 删除我的场景
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteLocationMy(Long userId, Long id);

    /**
     * 生成我的场景的候选图：同步校验模型后置为生成中，异步调用绘画模型并转存
     *
     * @param userId 用户编号
     * @param reqVO 请求：id 为场景编号
     */
    void generateLocationImageMy(Long userId, AiDramaImageGenerateReqVO reqVO);

    /**
     * 重新提交已失败的场景：使用上次的绘画模型
     *
     * @param id 编号
     */
    void resubmitLocationImage(Long id);

    /**
     * 获得场景的生成任务分页：生成过的场景
     *
     * @param pageParam 分页参数
     * @param reqVO 筛选条件
     * @param projectIds 项目编号；为空表示不过滤
     * @return 分页
     */
    PageResult<AiDramaLocationDO> getLocationTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                      Collection<Long> projectIds);

    /**
     * 把生成中的场景标记为已失败
     *
     * @param id 编号
     * @param errorMessage 失败原因
     */
    void updateLocationFail(Long id, String errorMessage);

    // ========== 视觉资产 ==========

    /**
     * 获得项目的视觉资产列表
     *
     * @param projectId 项目编号
     * @return 视觉资产列表
     */
    List<AiDramaVisualAssetDO> getVisualAssetList(Long projectId);

    /**
     * 获得视觉资产
     *
     * @param id 编号
     * @return 视觉资产
     */
    AiDramaVisualAssetDO getVisualAsset(Long id);

    /**
     * 获得视觉资产列表
     *
     * @param ids 编号
     * @return 视觉资产列表
     */
    List<AiDramaVisualAssetDO> getVisualAssetList(Collection<Long> ids);

    /**
     * 创建我的视觉资产：类型为空时为道具
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVisualAssetMy(Long userId, AiDramaVisualAssetSaveReqVO createReqVO);

    /**
     * 创建视觉资产：不校验项目归属，用于从剧本分析资产
     *
     * @param visualAsset 视觉资产
     * @return 编号
     */
    Long createVisualAsset(AiDramaVisualAssetDO visualAsset);

    /**
     * 修改我的视觉资产：类型为空时为道具
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateVisualAssetMy(Long userId, AiDramaVisualAssetSaveReqVO updateReqVO);

    /**
     * 删除我的视觉资产
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteVisualAssetMy(Long userId, Long id);

    /**
     * 生成我的视觉资产的图片：同步校验模型后置为生成中，异步调用绘画模型并转存
     *
     * @param userId 用户编号
     * @param reqVO 请求：id 为视觉资产编号
     */
    void generateVisualAssetImageMy(Long userId, AiDramaImageGenerateReqVO reqVO);

    /**
     * 重新提交已失败的视觉资产：使用上次的绘画模型
     *
     * @param id 编号
     */
    void resubmitVisualAssetImage(Long id);

    /**
     * 获得视觉资产的生成任务分页：生成过的视觉资产
     *
     * @param pageParam 分页参数
     * @param reqVO 筛选条件
     * @param projectIds 项目编号；为空表示不过滤
     * @return 分页
     */
    PageResult<AiDramaVisualAssetDO> getVisualAssetTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                            Collection<Long> projectIds);

    /**
     * 把生成中的视觉资产标记为已失败
     *
     * @param id 编号
     * @param errorMessage 失败原因
     */
    void updateVisualAssetFail(Long id, String errorMessage);

    /**
     * 一键补齐：没有图片、且不在生成中的角色形象、场景、视觉资产，全部提交出图
     *
     * @param userId 用户编号
     * @param reqVO 请求：id 为项目编号
     * @return 提交的数量
     */
    int generateMissingImagesMy(Long userId, AiDramaImageGenerateReqVO reqVO);

    /**
     * 出图超时处理：生成中且早于指定时间未更新的角色形象、场景、视觉资产，置为已失败（例如服务重启，异步任务丢失）
     *
     * @param updateTime 更新时间
     * @param errorMessage 失败原因
     * @return 处理的数量
     */
    int timeoutImage(LocalDateTime updateTime, String errorMessage);

    /**
     * 删除项目的全部资产：角色、角色形象、场景、视觉资产、音频
     *
     * @param projectId 项目编号
     */
    void deleteAssetListByProjectId(Long projectId);

    // ========== 音频 ==========

    /**
     * 获得项目的音频列表：角色样音、旁白
     *
     * @param projectId 项目编号
     * @return 音频列表
     */
    List<AiDramaAudioDO> getAudioList(Long projectId);

    /**
     * 获得项目角色的样音 Map：不包含旁白
     *
     * @param projectId 项目编号
     * @return 样音 Map：key 为角色编号
     */
    default Map<Long, List<AiDramaAudioDO>> getAudioMapByProjectId(Long projectId) {
        return convertMultiMap(filterList(getAudioList(projectId), audio -> audio.getCharacterId() != null),
                AiDramaAudioDO::getCharacterId);
    }

    /**
     * 获得音频
     *
     * @param id 编号
     * @return 音频
     */
    AiDramaAudioDO getAudio(Long id);

    /**
     * 上传我的音频：角色样音、旁白
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAudioMy(Long userId, AiDramaAudioCreateReqVO createReqVO);

    /**
     * 使用语音模型生成我的音频：有角色时为样音，否则为旁白；同步合成
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 编号
     */
    Long generateAudioMy(Long userId, AiDramaAudioGenerateReqVO reqVO);

    /**
     * 选用我的样音：同一角色只能选用一个
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void selectAudioMy(Long userId, Long id);

    /**
     * 删除我的音频
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteAudioMy(Long userId, Long id);

}
// DONE @AI：方法注释要补齐噢。（已补齐全部方法注释）