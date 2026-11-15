package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaAudioDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI 短剧音频 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiDramaAudioMapper extends BaseMapperX<AiDramaAudioDO> {

    default List<AiDramaAudioDO> selectListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<AiDramaAudioDO>()
                .eq(AiDramaAudioDO::getProjectId, projectId)
                .orderByAsc(AiDramaAudioDO::getId));
    }

    default int deleteByProjectId(Long projectId) {
        return delete(AiDramaAudioDO::getProjectId, projectId);
    }

    default int deleteByCharacterId(Long characterId) {
        return delete(AiDramaAudioDO::getCharacterId, characterId);
    }

    /**
     * 把角色的样音全部更新为指定类型：选用样音前，先把原来选用的改回普通样音
     */
    // DONE @AI：风格应该是 updateObj 噢，结尾字段噢；（已改为 updateObj 放最后，调用方一起调整）
    default int updateByCharacterIdAndType(Long characterId, Collection<Integer> whereTypes, AiDramaAudioDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<AiDramaAudioDO>()
                .eq(AiDramaAudioDO::getCharacterId, characterId)
                .in(AiDramaAudioDO::getType, whereTypes));
    }

}
