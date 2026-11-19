package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaDirectorDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 短剧 3D 导演工程 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaDirectorMapper extends BaseMapperX<AiDramaDirectorDO> {

    default AiDramaDirectorDO selectByProjectIdAndNodeId(Long projectId, String nodeId) {
        return selectOne(AiDramaDirectorDO::getProjectId, projectId, AiDramaDirectorDO::getNodeId, nodeId);
    }

    default int updateByIdAndRevision(Long id, Long whereRevision, AiDramaDirectorDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaDirectorDO>()
                .eq(AiDramaDirectorDO::getId, id).eq(AiDramaDirectorDO::getRevision, whereRevision));
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaDirectorDO::getProjectId, projectId);
    }

}
