package cn.iocoder.yudao.module.ai.dal.mysql.drama;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaAudioDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

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
     * 修改配乐、参考音频：使用的分镜为空时清空，表示候选（updateById 会忽略空值）
     */
    default void updateForSave(AiDramaAudioDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<AiDramaAudioDO>()
                .eq(AiDramaAudioDO::getId, updateObj.getId())
                .set(updateObj.getStoryboardIds() == null, AiDramaAudioDO::getStoryboardIds, null));
    }

    default List<AiDramaAudioDO> selectListByProjectIdAndType(Long projectId, Integer type) {
        return selectList(new LambdaQueryWrapperX<AiDramaAudioDO>()
                .eq(AiDramaAudioDO::getProjectId, projectId)
                .eq(AiDramaAudioDO::getType, type)
                .orderByAsc(AiDramaAudioDO::getId));
    }

}
