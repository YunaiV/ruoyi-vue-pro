package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCanvasDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 短剧画布 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaCanvasMapper extends BaseMapperX<AiDramaCanvasDO> {

    default AiDramaCanvasDO selectByProjectId(Long projectId) {
        return selectOne(AiDramaCanvasDO::getProjectId, projectId);
    }

    default int updateByIdAndRevision(Long id, Long whereRevision, AiDramaCanvasDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaCanvasDO>()
                .eq(AiDramaCanvasDO::getId, id).eq(AiDramaCanvasDO::getRevision, whereRevision));
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaCanvasDO::getProjectId, projectId);
    }

}
