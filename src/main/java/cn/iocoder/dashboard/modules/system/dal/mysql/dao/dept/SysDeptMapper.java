package cn.iocoder.dashboard.modules.system.dal.mysql.dao.dept;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDeptDO> {

    default List<SysDeptDO> selectList() {
        return selectList(null);
    }

}
