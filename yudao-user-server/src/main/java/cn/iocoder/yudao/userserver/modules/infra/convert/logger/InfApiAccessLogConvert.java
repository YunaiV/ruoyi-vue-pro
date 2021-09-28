package cn.iocoder.yudao.userserver.modules.infra.convert.logger;

import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.yudao.userserver.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * API 访问日志 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfApiAccessLogConvert {

    InfApiAccessLogConvert INSTANCE = Mappers.getMapper(InfApiAccessLogConvert.class);

    InfApiAccessLogDO convert(ApiAccessLogCreateDTO bean);

}
