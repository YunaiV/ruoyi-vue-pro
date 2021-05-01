package cn.iocoder.dashboard.modules.infra.convert.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.apilog.core.service.dto.ApiAccessLogCreateDTO;
import cn.iocoder.dashboard.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogExcelVO;
import cn.iocoder.dashboard.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogRespVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * API 访问日志 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfApiAccessLogConvert {

    InfApiAccessLogConvert INSTANCE = Mappers.getMapper(InfApiAccessLogConvert.class);

    InfApiAccessLogDO convert(ApiAccessLogCreateDTO bean);

    InfApiAccessLogRespVO convert(InfApiAccessLogDO bean);

    List<InfApiAccessLogRespVO> convertList(List<InfApiAccessLogDO> list);

    PageResult<InfApiAccessLogRespVO> convertPage(PageResult<InfApiAccessLogDO> page);

    List<InfApiAccessLogExcelVO> convertList02(List<InfApiAccessLogDO> list);

}
