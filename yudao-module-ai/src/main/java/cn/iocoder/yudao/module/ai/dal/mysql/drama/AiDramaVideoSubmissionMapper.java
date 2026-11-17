package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaVideoSubmissionDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 短剧视频提交记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaVideoSubmissionMapper extends BaseMapperX<AiDramaVideoSubmissionDO> {

    default AiDramaVideoSubmissionDO selectByRequestId(String requestId) {
        return selectOne(AiDramaVideoSubmissionDO::getRequestId, requestId);
    }

    default List<AiDramaVideoSubmissionDO> selectListByStoryboardId(Long storyboardId) {
        return selectList(new LambdaQueryWrapperX<AiDramaVideoSubmissionDO>()
                .eq(AiDramaVideoSubmissionDO::getStoryboardId, storyboardId)
                .orderByDesc(AiDramaVideoSubmissionDO::getId));
    }

    default AiDramaVideoSubmissionDO selectByStoryboardIdAndTaskId(Long storyboardId, String taskId) {
        return CollUtil.getFirst(selectList(new LambdaQueryWrapper<AiDramaVideoSubmissionDO>()
                .eq(AiDramaVideoSubmissionDO::getStoryboardId, storyboardId)
                .and(w -> w.eq(AiDramaVideoSubmissionDO::getTaskId, taskId)
                        .or(o -> o.isNull(AiDramaVideoSubmissionDO::getTaskId).eq(AiDramaVideoSubmissionDO::getRequestId, taskId)))
                .orderByDesc(AiDramaVideoSubmissionDO::getId)));
    }

    default int updateByStoryboardIdAndTaskId(Long storyboardId, String taskId, AiDramaVideoSubmissionDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaVideoSubmissionDO>()
                .eq(AiDramaVideoSubmissionDO::getStoryboardId, storyboardId)
                .and(w -> w.eq(AiDramaVideoSubmissionDO::getTaskId, taskId)
                        .or(o -> o.isNull(AiDramaVideoSubmissionDO::getTaskId).eq(AiDramaVideoSubmissionDO::getRequestId, taskId))));
    }

    default int updateByIdAndStatus(Long id, Integer whereStatus, AiDramaVideoSubmissionDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaVideoSubmissionDO>()
                .eq(AiDramaVideoSubmissionDO::getId, id).eq(AiDramaVideoSubmissionDO::getStatus, whereStatus));
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaVideoSubmissionDO::getProjectId, projectId);
    }

    default int deleteByStoryboardIds(List<Long> storyboardIds) {
        return delete(new LambdaQueryWrapperX<AiDramaVideoSubmissionDO>()
                .in(AiDramaVideoSubmissionDO::getStoryboardId, storyboardIds));
    }

}
