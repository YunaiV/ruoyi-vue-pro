package cn.iocoder.yudao.userserver.modules.system.convert.logger;

import cn.iocoder.yudao.userserver.modules.system.dal.mysql.logger.SysLoginLogDO;
import cn.iocoder.yudao.userserver.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysLoginLogConvert {

    SysLoginLogConvert INSTANCE = Mappers.getMapper(SysLoginLogConvert.class);

    SysLoginLogDO convert(SysLoginLogCreateReqDTO bean);

}
