package cn.iocoder.dashboard.modules.system.convert.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogExcelVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogRespVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysLoginLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysLoginLogConvert {

    SysLoginLogConvert INSTANCE = Mappers.getMapper(SysLoginLogConvert.class);

    SysLoginLogDO convert(SysLoginLogCreateReqVO bean);

    PageResult<SysLoginLogRespVO> convertPage(PageResult<SysLoginLogDO> page);

    List<SysLoginLogExcelVO> convertList(List<SysLoginLogDO> list);

}
