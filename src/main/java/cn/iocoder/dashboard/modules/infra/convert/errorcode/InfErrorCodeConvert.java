package cn.iocoder.dashboard.modules.infra.convert.errorcode;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.errorcode.core.dto.ErrorCodeAutoGenerateReqDTO;
import cn.iocoder.dashboard.framework.errorcode.core.dto.ErrorCodeRespDTO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeExcelVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeRespVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.errorcode.InfErrorCodeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 错误码 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfErrorCodeConvert {

    InfErrorCodeConvert INSTANCE = Mappers.getMapper(InfErrorCodeConvert.class);

    InfErrorCodeDO convert(InfErrorCodeCreateReqVO bean);

    InfErrorCodeDO convert(InfErrorCodeUpdateReqVO bean);

    InfErrorCodeRespVO convert(InfErrorCodeDO bean);

    List<InfErrorCodeRespVO> convertList(List<InfErrorCodeDO> list);

    PageResult<InfErrorCodeRespVO> convertPage(PageResult<InfErrorCodeDO> page);

    List<InfErrorCodeExcelVO> convertList02(List<InfErrorCodeDO> list);

    InfErrorCodeDO convert(ErrorCodeAutoGenerateReqDTO bean);

    List<ErrorCodeRespDTO> convertList03(List<InfErrorCodeDO> list);

}
