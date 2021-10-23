package cn.iocoder.yudao.coreservice.modules.infra.convert.logger;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InfApiAccessLogCoreConvert {

    InfApiAccessLogCoreConvert INSTANCE = Mappers.getMapper(InfApiAccessLogCoreConvert.class);

    InfApiAccessLogDO convert(ApiAccessLogCreateDTO bean);

}
