package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardVideoReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;

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
     * 替换项目的全部分镜：在事务中删除旧的、插入新的；用于由剧本拆分分镜
     *
     * @param projectId 项目编号
     * @param storyboards 新的分镜列表
     */
    void replaceStoryboardList(Long projectId, List<AiDramaStoryboardDO> storyboards);

    /**
     * 删除项目的全部分镜
     *
     * @param projectId 项目编号
     */
    void deleteStoryboardListByProjectId(Long projectId);

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

}
