package cn.iocoder.yudao.userserver.modules.system.dal.dataobject.logger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.userserver.modules.system.dal.mysql.logger.SysLoginLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysLoginLogMapper extends BaseMapperX<SysLoginLogDO> {

}
