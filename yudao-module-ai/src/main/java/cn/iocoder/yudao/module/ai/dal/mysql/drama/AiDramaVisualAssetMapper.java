package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaVisualAssetDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 短剧视觉资产 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaVisualAssetMapper extends BaseMapperX<AiDramaVisualAssetDO> {

    default List<AiDramaVisualAssetDO> selectListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .eq(AiDramaVisualAssetDO::getProjectId, projectId)
                .orderByAsc(AiDramaVisualAssetDO::getId));
    }

    /**
     * 获得项目的当前资产：不含历史版本
     */
    default List<AiDramaVisualAssetDO> selectListByProjectIdAndKindAndParentIdIsNull(Long projectId, Integer kind) {
        return selectList(new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .eq(AiDramaVisualAssetDO::getProjectId, projectId)
                .eq(AiDramaVisualAssetDO::getKind, kind)
                .isNull(AiDramaVisualAssetDO::getParentId)
                .orderByAsc(AiDramaVisualAssetDO::getId));
    }

    /**
     * 获得镜头的当前关键帧：不含历史版本
     */
    default AiDramaVisualAssetDO selectByStoryboardIdAndKindAndParentIdIsNull(Long storyboardId, Integer kind) {
        return selectFirstOne(new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .eq(AiDramaVisualAssetDO::getStoryboardId, storyboardId)
                .eq(AiDramaVisualAssetDO::getKind, kind)
                .isNull(AiDramaVisualAssetDO::getParentId)
                .orderByDesc(AiDramaVisualAssetDO::getId));
    }

    default List<AiDramaVisualAssetDO> selectListByStoryboardIdsAndKindAndParentIdIsNull(Collection<Long> storyboardIds,
                                                                                         Integer kind) {
        return selectList(new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .in(AiDramaVisualAssetDO::getStoryboardId, storyboardIds)
                .eq(AiDramaVisualAssetDO::getKind, kind)
                .isNull(AiDramaVisualAssetDO::getParentId));
    }

    /**
     * 获得最近一个历史版本
     */
    default AiDramaVisualAssetDO selectLatestByParentId(Long parentId) {
        return selectFirstOne(new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .eq(AiDramaVisualAssetDO::getParentId, parentId)
                .orderByDesc(AiDramaVisualAssetDO::getId));
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaVisualAssetDO::getProjectId, projectId);
    }

    default int deleteByParentId(Long parentId) {
        return delete(AiDramaVisualAssetDO::getParentId, parentId);
    }

    default int deleteByStoryboardIds(Collection<Long> storyboardIds) {
        return delete(new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .in(AiDramaVisualAssetDO::getStoryboardId, storyboardIds));
    }

    /**
     * 生成任务：生成过的视觉资产
     *
     * @param projectIds 项目编号；为空表示不过滤
     */
    default PageResult<AiDramaVisualAssetDO> selectTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                   Collection<Long> projectIds) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .inIfPresent(AiDramaVisualAssetDO::getProjectId, projectIds)
                .eqIfPresent(AiDramaVisualAssetDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiDramaVisualAssetDO::getModelId, reqVO.getModelId())
                .betweenIfPresent(AiDramaVisualAssetDO::getUpdateTime, reqVO.getUpdateTime())
                .isNotNull(AiDramaVisualAssetDO::getStatus)
                .isNull(AiDramaVisualAssetDO::getParentId)
                .orderByDesc(AiDramaVisualAssetDO::getUpdateTime)
                .orderByDesc(AiDramaVisualAssetDO::getId));
    }

    default List<AiDramaVisualAssetDO> selectListByStatusAndUpdateTimeLt(Integer status, LocalDateTime updateTime) {
        return selectList(new LambdaQueryWrapperX<AiDramaVisualAssetDO>()
                .eq(AiDramaVisualAssetDO::getStatus, status)
                .lt(AiDramaVisualAssetDO::getUpdateTime, updateTime));
    }

    /**
     * 按出图任务的条件更新：只更新状态未变、且仍是同一出图任务的记录（被标记失败后又重新提交时，任务编号已变）；任务编号为空时匹配为空的
     */
    default int updateByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId, AiDramaVisualAssetDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaVisualAssetDO>()
                .eq(AiDramaVisualAssetDO::getId, id)
                .eq(AiDramaVisualAssetDO::getStatus, whereStatus)
                .eq(whereTaskId != null, AiDramaVisualAssetDO::getTaskId, whereTaskId)
                .isNull(whereTaskId == null, AiDramaVisualAssetDO::getTaskId));
    }

    /**
     * 撤销时恢复版本：以状态、任务编号为条件，期间重新出图时不覆盖；状态为空时匹配为空的；失败原因一并清空
     */
    default int updateVersionByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId,
                                                    AiDramaVisualAssetDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaVisualAssetDO>()
                .eq(AiDramaVisualAssetDO::getId, id)
                .eq(whereStatus != null, AiDramaVisualAssetDO::getStatus, whereStatus)
                .isNull(whereStatus == null, AiDramaVisualAssetDO::getStatus)
                .eq(whereTaskId != null, AiDramaVisualAssetDO::getTaskId, whereTaskId)
                .isNull(whereTaskId == null, AiDramaVisualAssetDO::getTaskId)
                .set(updateObj.getImageUrl() == null, AiDramaVisualAssetDO::getImageUrl, null)
                .set(updateObj.getErrorMessage() == null, AiDramaVisualAssetDO::getErrorMessage, null));
    }

}
