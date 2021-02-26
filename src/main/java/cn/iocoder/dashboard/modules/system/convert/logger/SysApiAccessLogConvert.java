package cn.iocoder.dashboard.modules.system.convert.logger;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogExcelVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogRespVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysApiAccessLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * API 访问日志 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SysApiAccessLogConvert {

    SysApiAccessLogConvert INSTANCE = Mappers.getMapper(SysApiAccessLogConvert.class);

    SysApiAccessLogDO convert(ApiAccessLogCreateDTO bean);

    SysApiAccessLogRespVO convert(SysApiAccessLogDO bean);

    List<SysApiAccessLogRespVO> convertList(List<SysApiAccessLogDO> list);

    PageResult<SysApiAccessLogRespVO> convertPage(PageResult<SysApiAccessLogDO> page);

    List<SysApiAccessLogExcelVO> convertList02(List<SysApiAccessLogDO> list);

}
