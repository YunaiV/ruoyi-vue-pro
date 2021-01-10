package cn.iocoder.dashboard.modules.system.dal.mysql.dao.dept;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysPostDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SysPostMapper extends BaseMapper<SysPostDO> {

    default List<SysPostDO> selectList(Collection<Long> ids, Collection<Integer> statuses) {
        return selectList(new QueryWrapperX<SysPostDO>().inIfPresent("id", ids)
                .inIfPresent("status", statuses));
    }

}
