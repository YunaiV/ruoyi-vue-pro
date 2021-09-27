package cn.iocoder.yudao.userserver.modules.system.dal.mysql.dict;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.dict.SysDictDataDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface SysDictDataMapper extends BaseMapperX<SysDictDataDO> {

    default SysDictDataDO selectByDictTypeAndValue(String dictType, String value) {
        return selectOne(new QueryWrapper<SysDictDataDO>().eq("dict_type", dictType)
                .eq("value", value));
    }

    default int selectCountByDictType(String dictType) {
        return selectCount("dict_type", dictType);
    }

    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new QueryWrapper<SysDictDataDO>().select("id")
                .gt("update_time", maxUpdateTime).last("LIMIT 1")) != null;
    }

}
