package cn.iocoder.yudao.module.system.convert.logger;

import cn.iocoder.yudao.module.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.module.system.service.logger.dto.LoginLogCreateReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysLoginLogCoreConvert {

    SysLoginLogCoreConvert INSTANCE = Mappers.getMapper(SysLoginLogCoreConvert.class);

    SysLoginLogDO convert(LoginLogCreateReqDTO bean);

}
