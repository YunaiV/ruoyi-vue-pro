package cn.iocoder.dashboard.modules.system.dal.mysql.dao.logger;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysOperateLogDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperateLogMapper extends BaseMapper<SysOperateLogDO> {
}
