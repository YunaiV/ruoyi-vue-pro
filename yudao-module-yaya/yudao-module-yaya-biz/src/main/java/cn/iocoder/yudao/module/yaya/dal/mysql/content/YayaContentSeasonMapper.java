package cn.iocoder.yudao.module.yaya.dal.mysql.content;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaContentSeasonDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface YayaContentSeasonMapper extends BaseMapperX<YayaContentSeasonDO> {

    default YayaContentSeasonDO selectBySeasonKey(String seasonKey) {
        return selectOne(YayaContentSeasonDO::getSeasonKey, seasonKey);
    }

}
