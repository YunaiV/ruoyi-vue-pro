package cn.iocoder.yudao.module.infra.dal.mysql.logger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfApiErrorLogCoreMapper extends BaseMapperX<InfApiErrorLogDO> {
}
