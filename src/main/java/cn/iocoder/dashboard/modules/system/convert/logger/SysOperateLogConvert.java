package cn.iocoder.dashboard.modules.system.convert.logger;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.SysOperateLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.SysOperateLogRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysOperateLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysOperateLogConvert {

    SysOperateLogConvert INSTANCE = Mappers.getMapper(SysOperateLogConvert.class);

    SysOperateLogDO convert(SysOperateLogCreateReqVO bean);

    PageResult<SysOperateLogRespVO> convertPage(PageResult<SysOperateLogDO> page);

    SysOperateLogRespVO convert(SysOperateLogDO bean);

}
