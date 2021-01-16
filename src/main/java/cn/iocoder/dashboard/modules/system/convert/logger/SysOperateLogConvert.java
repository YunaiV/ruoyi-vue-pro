package cn.iocoder.dashboard.modules.system.convert.logger;

import cn.iocoder.dashboard.modules.system.controller.logger.vo.SysOperateLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysOperateLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysOperateLogConvert {

    SysOperateLogConvert INSTANCE = Mappers.getMapper(SysOperateLogConvert.class);

    SysOperateLogDO convert(SysOperateLogCreateReqVO bean);

}
