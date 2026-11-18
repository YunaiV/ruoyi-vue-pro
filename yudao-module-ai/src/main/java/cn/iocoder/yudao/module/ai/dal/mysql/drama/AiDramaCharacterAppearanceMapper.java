package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCharacterAppearanceDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 短剧角色形象 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaCharacterAppearanceMapper extends BaseMapperX<AiDramaCharacterAppearanceDO> {

    default List<AiDramaCharacterAppearanceDO> selectListByCharacterId(Long characterId) {
        return selectList(new LambdaQueryWrapperX<AiDramaCharacterAppearanceDO>()
                .eq(AiDramaCharacterAppearanceDO::getCharacterId, characterId)
                .orderByAsc(AiDramaCharacterAppearanceDO::getAppearanceIndex));
    }

    default List<AiDramaCharacterAppearanceDO> selectListByCharacterIds(Collection<Long> characterIds) {
        return selectList(new LambdaQueryWrapperX<AiDramaCharacterAppearanceDO>()
                .in(AiDramaCharacterAppearanceDO::getCharacterId, characterIds)
                .orderByAsc(AiDramaCharacterAppearanceDO::getAppearanceIndex));
    }

    default int deleteByCharacterIds(Collection<Long> characterIds) {
        return delete(new LambdaQueryWrapperX<AiDramaCharacterAppearanceDO>()
                .in(AiDramaCharacterAppearanceDO::getCharacterId, characterIds));
    }

    /**
     * 生成任务：生成过的角色形象
     *
     * @param characterIds 角色编号；为空表示不过滤
     */
    default PageResult<AiDramaCharacterAppearanceDO> selectTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                                    Collection<Long> characterIds) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiDramaCharacterAppearanceDO>()
                .inIfPresent(AiDramaCharacterAppearanceDO::getCharacterId, characterIds)
                .eqIfPresent(AiDramaCharacterAppearanceDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiDramaCharacterAppearanceDO::getModelId, reqVO.getModelId())
                .betweenIfPresent(AiDramaCharacterAppearanceDO::getUpdateTime, reqVO.getUpdateTime())
                .isNotNull(AiDramaCharacterAppearanceDO::getStatus)
                .orderByDesc(AiDramaCharacterAppearanceDO::getUpdateTime)
                .orderByDesc(AiDramaCharacterAppearanceDO::getId));
    }

    default List<AiDramaCharacterAppearanceDO> selectListByStatusAndUpdateTimeLt(Integer status, LocalDateTime updateTime) {
        return selectList(new LambdaQueryWrapperX<AiDramaCharacterAppearanceDO>()
                .eq(AiDramaCharacterAppearanceDO::getStatus, status)
                .lt(AiDramaCharacterAppearanceDO::getUpdateTime, updateTime));
    }

    /**
     * 按出图任务的条件更新：只更新状态未变、且仍是同一出图任务的记录（被标记失败后又重新提交时，任务编号已变）；任务编号为空时匹配为空的
     */
    default int updateByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId, AiDramaCharacterAppearanceDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaCharacterAppearanceDO>()
                .eq(AiDramaCharacterAppearanceDO::getId, id)
                .eq(AiDramaCharacterAppearanceDO::getStatus, whereStatus)
                .eq(whereTaskId != null, AiDramaCharacterAppearanceDO::getTaskId, whereTaskId)
                .isNull(whereTaskId == null, AiDramaCharacterAppearanceDO::getTaskId));
    }

    /**
     * 更新候选图：删除候选图、撤销时使用；选用、上一版为空时清空；以状态、任务编号为条件，期间重新出图时不覆盖
     */
    default int updateImagesByIdAndStatusAndTaskId(Long id, Integer whereStatus, String whereTaskId,
                                                   AiDramaCharacterAppearanceDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaCharacterAppearanceDO>()
                .eq(AiDramaCharacterAppearanceDO::getId, id)
                .eq(whereStatus != null, AiDramaCharacterAppearanceDO::getStatus, whereStatus)
                .isNull(whereStatus == null, AiDramaCharacterAppearanceDO::getStatus)
                .eq(whereTaskId != null, AiDramaCharacterAppearanceDO::getTaskId, whereTaskId)
                .isNull(whereTaskId == null, AiDramaCharacterAppearanceDO::getTaskId)
                .set(updateObj.getSelectedIndex() == null, AiDramaCharacterAppearanceDO::getSelectedIndex, null)
                .set(updateObj.getPreviousImageUrls() == null, AiDramaCharacterAppearanceDO::getPreviousImageUrls, null)
                .set(updateObj.getPreviousDescriptions() == null, AiDramaCharacterAppearanceDO::getPreviousDescriptions, null));
    }

}
