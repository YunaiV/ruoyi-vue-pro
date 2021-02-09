package cn.iocoder.dashboard.modules.system.dal.mysql.common;

import cn.iocoder.dashboard.modules.system.dal.dataobject.common.SysFileDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysFileMapper extends BaseMapper<SysFileDO> {

    default Integer selectCountById(String id) {
        return selectCount(new QueryWrapper<SysFileDO>().eq("id", id));
    }

}
