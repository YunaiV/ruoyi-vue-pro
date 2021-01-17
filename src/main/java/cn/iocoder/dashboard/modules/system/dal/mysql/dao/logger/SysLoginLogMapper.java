package cn.iocoder.dashboard.modules.system.dal.mysql.dao.logger;

import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysLoginLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysLoginLogMapper extends BaseMapperX<SysLoginLogDO> {
}
