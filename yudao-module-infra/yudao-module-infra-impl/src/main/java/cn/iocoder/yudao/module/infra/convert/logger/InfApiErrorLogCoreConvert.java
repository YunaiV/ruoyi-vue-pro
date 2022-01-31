package cn.iocoder.yudao.module.infra.convert.logger;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiErrorLogCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InfApiErrorLogCoreConvert {

    InfApiErrorLogCoreConvert INSTANCE = Mappers.getMapper(InfApiErrorLogCoreConvert.class);

    InfApiErrorLogDO convert(ApiErrorLogCreateReqDTO bean);

}
