package cn.iocoder.yudao.adminserver.modules.infra.convert.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiErrorLogCreateDTO;
import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apierrorlog.InfApiErrorLogExcelVO;
import cn.iocoder.yudao.adminserver.modules.infra.controller.logger.vo.apierrorlog.InfApiErrorLogRespVO;
import cn.iocoder.yudao.adminserver.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * API 错误日志 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfApiErrorLogConvert {

    InfApiErrorLogConvert INSTANCE = Mappers.getMapper(InfApiErrorLogConvert.class);

    InfApiErrorLogDO convert(ApiErrorLogCreateDTO bean);

    InfApiErrorLogRespVO convert(InfApiErrorLogDO bean);

    PageResult<InfApiErrorLogRespVO> convertPage(PageResult<InfApiErrorLogDO> page);

    List<InfApiErrorLogExcelVO> convertList02(List<InfApiErrorLogDO> list);

}
