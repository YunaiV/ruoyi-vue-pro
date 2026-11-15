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

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaVisualAssetDO::getProjectId, projectId);
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

}
