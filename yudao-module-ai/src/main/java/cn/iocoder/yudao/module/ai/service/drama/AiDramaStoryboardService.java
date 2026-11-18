package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaVideoSubmissionDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * AI 短剧分镜 Service 接口
 *
 * @author 芋道源码
 */
public interface AiDramaStoryboardService {

    /**
     * 获得项目的分镜列表：按场次排序
     *
     * @param projectId 项目编号
     * @return 分镜列表
     */
    List<AiDramaStoryboardDO> getStoryboardList(Long projectId);

    /**
     * 获得多个项目的分镜列表
     *
     * @param projectIds 项目编号
     * @return 分镜列表
     */
    List<AiDramaStoryboardDO> getStoryboardListByProjectIds(Collection<Long> projectIds);

    /**
     * 获得多个项目的分镜 Map
     *
     * @param projectIds 项目编号
     * @return 分镜 Map：key 为项目编号
     */
    default Map<Long, List<AiDramaStoryboardDO>> getStoryboardMapByProjectIds(Collection<Long> projectIds) {
        return convertMultiMap(getStoryboardListByProjectIds(projectIds), AiDramaStoryboardDO::getProjectId);
    }

    /**
     * 获得分镜
     *
     * @param id 编号
     * @return 分镜
     */
    AiDramaStoryboardDO getStoryboard(Long id);

    /**
     * 获得【我的】分镜：包括历史剧本版本的分镜，用于核对画布节点绑定的镜头
     *
     * @param userId 用户编号
     * @param id     编号
     * @return 分镜；不存在时为空
     */
    AiDramaStoryboardDO getStoryboardMy(Long userId, Long id);

    /**
     * 获得【我的】剧本版本的分镜列表：按镜头序号排序，用于只读查看历史版本
     *
     * @param userId   用户编号
     * @param scriptId 剧本编号
     * @return 分镜列表
     */
    List<AiDramaStoryboardDO> getStoryboardListByScriptIdMy(Long userId, Long scriptId);

    /**
     * 校验镜头视频不在生成中：生成期间不能修改关键帧等起始帧的来源；分镜不存在时不校验
     *
     * @param id 分镜编号
     */
    void validateStoryboardVideoNotRunning(Long id);

    /**
     * 创建我的分镜
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStoryboardMy(Long userId, AiDramaStoryboardSaveReqVO createReqVO);

    /**
     * 修改我的分镜：镜头视频生成中时不允许
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateStoryboardMy(Long userId, AiDramaStoryboardSaveReqVO updateReqVO);

    /**
     * 删除我的分镜
     *
     * @param userId 用户编号
     * @param id 编号
     */
    void deleteStoryboardMy(Long userId, Long id);

    /**
     * 替换剧本的分镜：在事务中删除该剧本旧的、插入新的，其它剧本版本的分镜保留；用于由剧本拆分分镜
     *
     * @param projectId 项目编号
     * @param scriptId 剧本编号
     * @param storyboards 新的分镜列表
     */
    void replaceStoryboardList(Long projectId, Long scriptId, List<AiDramaStoryboardDO> storyboards);

    /**
     * 删除项目的全部分镜
     *
     * @param projectId 项目编号
     */
    void deleteStoryboardListByProjectId(Long projectId);

    /**
     * 批量更新分镜：用于摄影规则、表演指导、关键帧规划的生成结果
     *
     * @param updateList 更新的分镜，需要有编号
     */
    void updateStoryboardList(List<AiDramaStoryboardDO> updateList);

    /**
     * 生成镜头视频：同步提交服务商，之后由 Job 查询结果
     *
     * @param userId 用户编号
     * @param reqVO 请求：id 为分镜编号
     */
    void generateStoryboardVideoMy(Long userId, AiDramaStoryboardVideoReqVO reqVO);

    /**
     * 批量生成镜头视频：未生成、已失败的镜头逐个提交；单个参数不通过的，记为已失败
     *
     * @param userId 用户编号
     * @param reqVO 请求：id 为项目编号
     * @return 处理的数量
     */
    int generateStoryboardVideoBatchMy(Long userId, AiDramaStoryboardVideoReqVO reqVO);

    /**
     * 重新提交已失败的镜头视频：使用上次的视频模型
     *
     * @param id 编号
     */
    void resubmitStoryboardVideo(Long id);

    /**
     * 同步生成中、转存中的镜头视频：查询服务商，成功后转存
     *
     * @return 有状态变化的数量
     */
    int syncStoryboardVideo();

    /**
     * 同步单个镜头视频
     *
     * @param id 编号
     */
    void syncStoryboardVideo(Long id);

    /**
     * 获得镜头视频的生成任务分页：生成过视频的镜头
     *
     * @param pageParam 分页参数
     * @param reqVO 筛选条件
     * @param projectIds 项目编号；为空表示不过滤
     * @return 分页
     */
    PageResult<AiDramaStoryboardDO> getStoryboardTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                          Collection<Long> projectIds);

    /**
     * 把进行中的镜头视频标记为已失败
     *
     * @param id 编号
     * @param errorMessage 失败原因
     */
    void updateStoryboardVideoFail(Long id, String errorMessage);

    /**
     * 镜头视频超时处理：待处理、生成中、转存中且早于指定时间未更新的，置为已失败
     *
     * @param updateTime 更新时间
     * @param errorMessage 失败原因
     * @return 处理的数量
     */
    int timeoutStoryboardVideo(LocalDateTime updateTime, String errorMessage);

    // ========== 镜头视频：提交记录 ==========

    /**
     * 获得我的镜头的视频提交记录：新的在前
     *
     * @param userId 用户编号
     * @param storyboardId 分镜编号
     * @return 提交记录
     */
    List<AiDramaVideoSubmissionDO> getVideoSubmissionListMy(Long userId, Long storyboardId);

    /**
     * 确认提交结果未知的提交记录的任务编号：分镜改为生成中，由 Job 查询结果
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void resolveVideoSubmissionMy(Long userId, AiDramaVideoSubmissionResolveReqVO reqVO);

    /**
     * 确认提交结果未知的提交记录为已失败：服务商未创建任务
     *
     * @param userId 用户编号
     * @param id 提交记录编号
     */
    void confirmVideoSubmissionFailMy(Long userId, Long id);

    /**
     * 使用提交记录的视频作为镜头视频
     *
     * @param userId 用户编号
     * @param id 提交记录编号
     * @param previous true 为恢复提交前的视频，false 为使用本次提交成功的视频
     */
    void useVideoSubmissionMy(Long userId, Long id, boolean previous);

    // ========== 关键帧、素材 ==========

    /**
     * 生成我的镜头关键帧：存为视觉资产（shot_frame 类型），同步校验模型后置为出图中，异步出图；有修正说明时，以当前关键帧为参考修正构图
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void generateStoryboardFrameMy(Long userId, AiDramaStoryboardFrameReqVO reqVO);

    /**
     * 批量生成我的镜头关键帧：当前剧本中镜头序号在范围内的镜头，已有关键帧、出图中、镜头视频生成中的跳过
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 提交的数量
     */
    int generateStoryboardFrameBatchMy(Long userId, AiDramaStoryboardFrameBatchReqVO reqVO);

    /**
     * 承接上一镜头：同一剧本版本中上一个镜头视频的尾帧，作为本镜头的手动起始帧
     *
     * @param userId 用户编号
     * @param id 分镜编号
     */
    void usePreviousLastFrameMy(Long userId, Long id);

    /**
     * 把我的镜头的素材图片渲染为镜头视频：按项目画幅、镜头时长，直接作为已完成的镜头视频
     *
     * @param userId 用户编号
     * @param id 分镜编号
     */
    void renderStoryboardMaterialMy(Long userId, Long id);

    /**
     * 导入外部视频，直接作为我的镜头的已完成视频：截取尾帧供下一镜承接
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void importStoryboardVideoMy(Long userId, AiDramaStoryboardImportVideoReqVO reqVO);

    // ========== 声音 ==========

    /**
     * 获得我的镜头的声音预览：生成镜头视频时使用的发言角色、参考音频
     *
     * @param userId 用户编号
     * @param id 分镜编号
     * @return 声音预览
     */
    AiDramaStoryboardVoiceRespVO getStoryboardVoiceMy(Long userId, Long id);

    /**
     * 设置我的镜头的发言角色：为空时按台词自动识别
     *
     * @param userId 用户编号
     * @param reqVO 请求
     */
    void updateStoryboardSpeakersMy(Long userId, AiDramaStoryboardSpeakerReqVO reqVO);

}
