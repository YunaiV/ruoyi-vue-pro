package cn.iocoder.yudao.module.system.convert.logger;

import cn.iocoder.yudao.module.system.controller.logger.vo.loginlog.SysLoginLogExcelVO;
import cn.iocoder.yudao.module.system.controller.logger.vo.loginlog.SysLoginLogRespVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysLoginLogConvert {

    SysLoginLogConvert INSTANCE = Mappers.getMapper(SysLoginLogConvert.class);

    PageResult<SysLoginLogRespVO> convertPage(PageResult<SysLoginLogDO> page);

    List<SysLoginLogExcelVO> convertList(List<SysLoginLogDO> list);

}
