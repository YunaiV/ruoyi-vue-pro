package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaScriptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 短剧剧本 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaScriptMapper extends BaseMapperX<AiDramaScriptDO> {

    default AiDramaScriptDO selectByProjectId(Long projectId) {
        return selectFirstOne(AiDramaScriptDO::getProjectId, projectId);
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaScriptDO::getProjectId, projectId);
    }

}
