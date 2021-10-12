package cn.iocoder.yudao.coreservice.modules.infra.convert.logger;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiErrorLogCreateDTO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InfApiErrorLogCoreConvert {

    InfApiErrorLogCoreConvert INSTANCE = Mappers.getMapper(InfApiErrorLogCoreConvert.class);

    InfApiErrorLogDO convert(ApiErrorLogCreateDTO bean);

}
