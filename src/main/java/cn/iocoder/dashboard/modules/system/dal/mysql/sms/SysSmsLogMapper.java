package cn.iocoder.dashboard.modules.system.dal.mysql.sms;

import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysSmsLogMapper extends BaseMapperX<SysSmsLogDO> {
}
