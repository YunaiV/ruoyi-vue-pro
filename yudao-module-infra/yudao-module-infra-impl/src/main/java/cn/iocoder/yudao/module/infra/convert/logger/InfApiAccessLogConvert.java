package cn.iocoder.yudao.module.infra.convert.logger;

import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.InfApiAccessLogExcelVO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.InfApiAccessLogRespVO;
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

    InfApiAccessLogRespVO convert(InfApiAccessLogDO bean);

    List<InfApiAccessLogRespVO> convertList(List<InfApiAccessLogDO> list);

    PageResult<InfApiAccessLogRespVO> convertPage(PageResult<InfApiAccessLogDO> page);

    List<InfApiAccessLogExcelVO> convertList02(List<InfApiAccessLogDO> list);

}
