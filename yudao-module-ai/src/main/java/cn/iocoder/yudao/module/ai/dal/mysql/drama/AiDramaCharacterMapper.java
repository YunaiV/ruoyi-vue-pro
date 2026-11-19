package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCharacterDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI 短剧角色 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaCharacterMapper extends BaseMapperX<AiDramaCharacterDO> {

    default List<AiDramaCharacterDO> selectListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<AiDramaCharacterDO>()
                .eq(AiDramaCharacterDO::getProjectId, projectId)
                .orderByAsc(AiDramaCharacterDO::getId));
    }

    default List<AiDramaCharacterDO> selectListByProjectIds(Collection<Long> projectIds) {
        return selectList(AiDramaCharacterDO::getProjectId, projectIds);
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaCharacterDO::getProjectId, projectId);
    }

}
