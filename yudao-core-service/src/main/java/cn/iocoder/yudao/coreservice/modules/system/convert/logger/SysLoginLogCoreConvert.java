package cn.iocoder.yudao.coreservice.modules.system.convert.logger;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysLoginLogCoreConvert {

    SysLoginLogCoreConvert INSTANCE = Mappers.getMapper(SysLoginLogCoreConvert.class);

    SysLoginLogDO convert(SysLoginLogCreateReqDTO bean);

}
