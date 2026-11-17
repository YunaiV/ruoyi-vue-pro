package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 短剧分镜 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaStoryboardMapper extends BaseMapperX<AiDramaStoryboardDO> {

    default List<AiDramaStoryboardDO> selectListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<AiDramaStoryboardDO>()
                .eq(AiDramaStoryboardDO::getProjectId, projectId)
                .orderByAsc(AiDramaStoryboardDO::getSceneNo)
                .orderByAsc(AiDramaStoryboardDO::getId));
    }

    default List<AiDramaStoryboardDO> selectListByProjectIds(Collection<Long> projectIds) {
        return selectList(AiDramaStoryboardDO::getProjectId, projectIds);
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaStoryboardDO::getProjectId, projectId);
    }

    default int deleteByProjectIdAndScriptId(Long projectId, Long scriptId) {
        return delete(new LambdaQueryWrapperX<AiDramaStoryboardDO>()
                .eq(AiDramaStoryboardDO::getProjectId, projectId)
                .eq(AiDramaStoryboardDO::getScriptId, scriptId));
    }

    default List<AiDramaStoryboardDO> selectListByVideoStatus(Collection<Integer> videoStatuses, int limit) {
        return selectListLimitN(new LambdaQueryWrapperX<AiDramaStoryboardDO>()
                .in(AiDramaStoryboardDO::getVideoStatus, videoStatuses)
                .orderByAsc(AiDramaStoryboardDO::getId), limit);
    }

    default List<AiDramaStoryboardDO> selectListByVideoStatusAndUpdateTimeLt(Collection<Integer> videoStatuses,
                                                                             LocalDateTime updateTime) {
        return selectList(new LambdaQueryWrapperX<AiDramaStoryboardDO>()
                .in(AiDramaStoryboardDO::getVideoStatus, videoStatuses)
                .lt(AiDramaStoryboardDO::getUpdateTime, updateTime));
    }

    /**
     * 生成任务：生成过视频的镜头
     *
     * @param projectIds 项目编号；为空表示不过滤
     */
    default PageResult<AiDramaStoryboardDO> selectTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                           Collection<Long> projectIds) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiDramaStoryboardDO>()
                .inIfPresent(AiDramaStoryboardDO::getProjectId, projectIds)
                .eqIfPresent(AiDramaStoryboardDO::getVideoStatus, reqVO.getStatus())
                .eqIfPresent(AiDramaStoryboardDO::getVideoModelId, reqVO.getModelId())
                .betweenIfPresent(AiDramaStoryboardDO::getUpdateTime, reqVO.getUpdateTime())
                .isNotNull(AiDramaStoryboardDO::getVideoStatus)
                .orderByDesc(AiDramaStoryboardDO::getUpdateTime)
                .orderByDesc(AiDramaStoryboardDO::getId));
    }

    /**
     * 编辑保存：场景、视频模型、道具为空时清空（updateById 会忽略空值）；只用于编辑，状态回写不经过这里，不会清掉它们
     *
     * 道具为空表示按道具的镜头序号匹配，空数组表示明确不使用道具，两者要区分保存
     */
    default void updateForSave(AiDramaStoryboardDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<AiDramaStoryboardDO>()
                .eq(AiDramaStoryboardDO::getId, updateObj.getId())
                .set(updateObj.getLocationId() == null, AiDramaStoryboardDO::getLocationId, null)
                .set(updateObj.getVideoModelId() == null, AiDramaStoryboardDO::getVideoModelId, null)
                .set(updateObj.getVisualAssetIds() == null, AiDramaStoryboardDO::getVisualAssetIds, null));
    }

    /**
     * 按任务编号的条件更新：只更新状态未变、且仍是同一轮提交的镜头（被标记失败后又重新提交时，任务编号已变）；状态、任务编号为空时匹配为空的
     */
    default int updateByIdAndVideoStatusAndTaskId(Long id, Integer whereVideoStatus, String whereTaskId, AiDramaStoryboardDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaStoryboardDO>()
                .eq(AiDramaStoryboardDO::getId, id)
                .eq(whereVideoStatus != null, AiDramaStoryboardDO::getVideoStatus, whereVideoStatus)
                .isNull(whereVideoStatus == null, AiDramaStoryboardDO::getVideoStatus)
                .eq(whereTaskId != null, AiDramaStoryboardDO::getTaskId, whereTaskId)
                .isNull(whereTaskId == null, AiDramaStoryboardDO::getTaskId));
    }

}
