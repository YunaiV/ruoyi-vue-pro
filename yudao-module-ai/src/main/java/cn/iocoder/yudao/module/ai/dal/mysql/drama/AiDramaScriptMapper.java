package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaScriptDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI 短剧剧本 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaScriptMapper extends BaseMapperX<AiDramaScriptDO> {

    /**
     * 获得项目的当前剧本：最新一条
     */
    default AiDramaScriptDO selectLatestByProjectId(Long projectId) {
        return selectFirstOne(new LambdaQueryWrapperX<AiDramaScriptDO>()
                .eq(AiDramaScriptDO::getProjectId, projectId)
                .orderByDesc(AiDramaScriptDO::getId));
    }

    /**
     * 获得项目的全部剧本版本：新的在前
     */
    default List<AiDramaScriptDO> selectListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<AiDramaScriptDO>()
                .eq(AiDramaScriptDO::getProjectId, projectId)
                .orderByDesc(AiDramaScriptDO::getId));
    }

    /**
     * 获得多个项目的剧本编号：只查编号、项目编号，不查正文
     */
    default List<AiDramaScriptDO> selectIdListByProjectIds(Collection<Long> projectIds) {
        return selectList(new LambdaQueryWrapperX<AiDramaScriptDO>()
                .select(AiDramaScriptDO::getId, AiDramaScriptDO::getProjectId)
                .in(AiDramaScriptDO::getProjectId, projectIds));
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaScriptDO::getProjectId, projectId);
    }

}
