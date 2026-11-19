package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaLocationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 短剧场景 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaLocationMapper extends BaseMapperX<AiDramaLocationDO> {

    default List<AiDramaLocationDO> selectListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<AiDramaLocationDO>()
                .eq(AiDramaLocationDO::getProjectId, projectId)
                .orderByAsc(AiDramaLocationDO::getId));
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaLocationDO::getProjectId, projectId);
    }

    /**
     * 生成任务：生成过的场景
     *
     * @param projectIds 项目编号；为空表示不过滤
     */
    default PageResult<AiDramaLocationDO> selectTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                         Collection<Long> projectIds) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiDramaLocationDO>()
                .inIfPresent(AiDramaLocationDO::getProjectId, projectIds)
                .eqIfPresent(AiDramaLocationDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiDramaLocationDO::getModelId, reqVO.getModelId())
                .betweenIfPresent(AiDramaLocationDO::getUpdateTime, reqVO.getUpdateTime())
                .isNotNull(AiDramaLocationDO::getStatus)
                .orderByDesc(AiDramaLocationDO::getUpdateTime)
                .orderByDesc(AiDramaLocationDO::getId));
    }

    default List<AiDramaLocationDO> selectListByStatusAndUpdateTimeLt(Integer status, LocalDateTime updateTime) {
        return selectList(new LambdaQueryWrapperX<AiDramaLocationDO>()
                .eq(AiDramaLocationDO::getStatus, status)
                .lt(AiDramaLocationDO::getUpdateTime, updateTime));
    }

    /**
     * 按出图任务的条件更新：只更新状态未变、且仍是同一出图任务的记录（被标记失败后又重新提交时，任务编号已变）；任务编号为空时匹配为空的
     */
    default int updateByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId, AiDramaLocationDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaLocationDO>()
                .eq(AiDramaLocationDO::getId, id)
                .eq(AiDramaLocationDO::getStatus, whereStatus)
                .eq(whereTaskId != null, AiDramaLocationDO::getTaskId, whereTaskId)
                .isNull(whereTaskId == null, AiDramaLocationDO::getTaskId));
    }

    /**
     * 更新候选图：删除候选图、撤销时使用；选用、上一版为空时清空；以状态、任务编号为条件，期间重新出图时不覆盖
     */
    default int updateImagesByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId,
                                                   AiDramaLocationDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaLocationDO>()
                .eq(AiDramaLocationDO::getId, id)
                .eq(whereStatus != null, AiDramaLocationDO::getStatus, whereStatus)
                .isNull(whereStatus == null, AiDramaLocationDO::getStatus)
                .eq(whereTaskId != null, AiDramaLocationDO::getTaskId, whereTaskId)
                .isNull(whereTaskId == null, AiDramaLocationDO::getTaskId)
                .set(updateObj.getSelectedIndex() == null, AiDramaLocationDO::getSelectedIndex, null)
                .set(updateObj.getPreviousImageUrls() == null, AiDramaLocationDO::getPreviousImageUrls, null)
                .set(updateObj.getPreviousDescriptions() == null, AiDramaLocationDO::getPreviousDescriptions, null));
    }

}
