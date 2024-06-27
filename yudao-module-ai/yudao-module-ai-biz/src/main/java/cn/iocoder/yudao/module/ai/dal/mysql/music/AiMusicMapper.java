package cn.iocoder.yudao.module.ai.dal.mysql.music;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 音乐 Mapper
 *
 * @author xiaoxin
 */
@Mapper
public interface AiMusicMapper extends BaseMapperX<AiMusicDO> {

    default List<AiMusicDO> selectListByStatus(Integer status) {
        return selectList(AiMusicDO::getStatus, status);
    }

}
