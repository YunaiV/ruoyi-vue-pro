package cn.iocoder.yudao.coreservice.modules.system.dal.mysql.dict;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dict.SysDictDataDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface SysDictDataCoreMapper extends BaseMapperX<SysDictDataDO> {

    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new QueryWrapper<SysDictDataDO>().select("id")
                .gt("update_time", maxUpdateTime).last("LIMIT 1")) != null;
    }

}
