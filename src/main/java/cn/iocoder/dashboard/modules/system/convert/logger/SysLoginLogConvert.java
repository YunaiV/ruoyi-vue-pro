package cn.iocoder.dashboard.modules.system.convert.logger;

import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysLoginLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysLoginLogConvert {

    SysLoginLogConvert INSTANCE = Mappers.getMapper(SysLoginLogConvert.class);

    SysLoginLogDO convert(SysLoginLogCreateReqVO bean);

}
