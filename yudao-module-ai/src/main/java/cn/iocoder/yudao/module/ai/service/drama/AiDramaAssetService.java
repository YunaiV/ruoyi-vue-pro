package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.motion.AiDramaMotionReferenceCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

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
     * 删除我的角色形象的一张候选图：出图中不允许；同步调整选用的候选图
     *
     * @param userId 用户编号
     * @param id 角色形象编号
     * @param index 候选图下标
     */
    void deleteAppearanceCandidateMy(Long userId, Long id, Integer index);

    /**
     * 撤销我的角色形象的上次候选图变更：恢复为上一版候选图，选用最后一张；只能撤销一次
     *
     * @param userId 用户编号
     * @param id 角色形象编号
     */
    void undoAppearanceImageMy(Long userId, Long id);

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
     * 删除我的场景的一张候选图：出图中不允许；同步调整选用的候选图
     *
     * @param userId 用户编号
     * @param id 场景编号
     * @param index 候选图下标
     */
    void deleteLocationCandidateMy(Long userId, Long id, Integer index);

    /**
     * 撤销我的场景的上次候选图变更：恢复为上一版候选图，选用最后一张；只能撤销一次
     *
     * @param userId 用户编号
     * @param id 场景编号
     */
    void undoLocationImageMy(Long userId, Long id);

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
     * 获得项目的视觉资产列表：包含道具、关键帧，以及它们的历史版本
     *
     * @param projectId 项目编号
     * @return 视觉资产列表
     */
    List<AiDramaVisualAssetDO> getVisualAssetList(Long projectId);

    /**
     * 获得项目的当前道具列表：不含历史版本
     *
     * @param projectId 项目编号
     * @return 道具列表
     */
    List<AiDramaVisualAssetDO> getPropList(Long projectId);

    /**
     * 获得镜头的当前关键帧
     *
     * @param storyboardId 分镜编号
     * @return 关键帧；不存在时为 null
     */
    AiDramaVisualAssetDO getStoryboardFrame(Long storyboardId);

    /**
     * 获得多个镜头的当前关键帧
     *
     * @param storyboardIds 分镜编号
     * @return 关键帧列表
     */
    List<AiDramaVisualAssetDO> getStoryboardFrameList(Collection<Long> storyboardIds);

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
     * 创建我的道具
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
     * 修改我的道具：历史版本、关键帧不能修改
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateVisualAssetMy(Long userId, AiDramaVisualAssetSaveReqVO updateReqVO);

    /**
     * 删除我的道具：历史版本一并删除
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
     * 撤销我的视觉资产（道具、关键帧）的上次出图：与最近的历史版本互换
     *
     * @param userId 用户编号
     * @param id 当前版本的编号
     */
    void undoVisualAssetImageMy(Long userId, Long id);

    /**
     * 归档视觉资产的当前版本：有图片时复制为一条历史版本，父编号指向当前版本；没有图片时不归档
     *
     * @param current 当前版本
     */
    void archiveVisualAsset(AiDramaVisualAssetDO current);

    /**
     * 按出图任务的条件更新视觉资产：只更新状态未变、且仍是同一出图任务的记录
     *
     * @param id 编号
     * @param whereStatus 状态条件
     * @param whereTaskId 任务编号条件
     * @param updateObj 更新内容
     * @return 更新的数量
     */
    int updateVisualAssetByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId,
                                                AiDramaVisualAssetDO updateObj);

    /**
     * 拼接出图的画风说明：画风、美学技能
     *
     * @param project 项目
     * @return 画风说明；没有时为空串
     */
    String buildStyleText(AiDramaProjectDO project);

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

    // ========== 出图：供镜头起始帧使用 ==========

    /**
     * 校验绘画模型可用于出图
     *
     * @param modelId 绘画模型编号
     */
    void validateImageModel(Long modelId);

    /**
     * 异步出图：在异步线程调用绘画模型并转存到文件服务，完成后回调
     *
     * @param modelId 绘画模型编号，需先通过 {@link #validateImageModel(Long)} 校验
     * @param prompt 提示词
     * @param referenceUrls 参考图地址：只有豆包 Seedream 支持，其它平台忽略
     * @param aspectRatio 画幅，例如 9:16：只有豆包 Seedream 按画幅出图，其它平台为正方形
     * @param callback 回调：参数为图片地址、失败原因，二者有且只有一个不为空
     */
    void drawImageAsync(Long modelId, String prompt, List<String> referenceUrls, String aspectRatio,
                        BiConsumer<String, String> callback);

    /**
     * 提交镜头关键帧出图：没有关键帧时新建；已有图片时先归档为历史版本，再记录提示词、参考图并置为生成中，异步出图
     *
     * @param storyboard 分镜
     * @param modelId 绘画模型编号
     * @param prompt 提示词
     * @param referenceImages 参考图：地址、道具版本、上一版关键帧
     * @param sourceHash 输入摘要：用于判断关键帧是否对应镜头当前的输入
     * @param aspectRatio 画幅
     */
    void generateStoryboardFrame(AiDramaStoryboardDO storyboard, Long modelId, String prompt,
                                 AiDramaVisualAssetDO.ReferenceImages referenceImages, String sourceHash,
                                 String aspectRatio);

    /**
     * 删除分镜的关键帧：当前版本与历史版本
     *
     * @param storyboardIds 分镜编号
     */
    void deleteStoryboardFrameList(Collection<Long> storyboardIds);

    // ========== 音频 ==========

    /**
     * 获得项目的音频列表
     *
     * @param projectId 项目编号
     * @return 音频列表
     */
    List<AiDramaAudioDO> getAudioList(Long projectId);

    /**
     * 获得项目角色的样音 Map：只包含有角色的音频
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
     * 上传我的音频：角色样音、旁白、配乐、参考音频
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
     * 选用我的样音：记录到角色的选用样音
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void selectAudioMy(Long userId, Long id);

    /**
     * 修改我的配乐、参考音频：名称、使用的分镜、音量、起始偏移
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateAudioMy(Long userId, AiDramaAudioUpdateReqVO updateReqVO);

    /**
     * 获得项目的参考音频列表
     *
     * @param projectId 项目编号
     * @return 参考音频列表
     */
    List<AiDramaAudioDO> getReferenceAudioList(Long projectId);

    /**
     * 删除我的音频：是角色选用的样音时，取消选用
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteAudioMy(Long userId, Long id);

    /**
     * 选用我的 AI 音乐为配乐：复制音乐的文件地址与时长，记录 AI 音乐编号
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 音频编号
     */
    Long createMusicAudioMy(Long userId, AiDramaAudioMusicReqVO reqVO);

    // ========== 动作参考 ==========

    /**
     * 获得项目的动作参考列表：新的在前
     *
     * @param projectId 项目编号
     * @return 动作参考列表
     */
    List<AiDramaMotionReferenceDO> getMotionReferenceList(Long projectId);

    /**
     * 创建我的动作参考：读取视频的时长、帧率、编码，不符合动作参考视频要求的拒绝
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMotionReferenceMy(Long userId, AiDramaMotionReferenceCreateReqVO createReqVO);

    /**
     * 删除我的动作参考：已选用它的分镜保留视频地址
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteMotionReferenceMy(Long userId, Long id);

}