package cn.iocoder.dashboard.modules.system.convert.errorcode;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.errorcode.core.dto.ErrorCodeAutoGenerateReqDTO;
import cn.iocoder.dashboard.framework.errorcode.core.dto.ErrorCodeRespDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodeCreateDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodeUpdateDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.vo.ErrorCodeVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.errorcode.ErrorCodeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface SysErrorCodeConvert {

    SysErrorCodeConvert INSTANCE = Mappers.getMapper(SysErrorCodeConvert.class);

    ErrorCodeVO convert (ErrorCodeDO bean);

    List<ErrorCodeVO> convertList(List<ErrorCodeDO> list);

    @Mapping(source = "records", target = "list")
    PageResult<ErrorCodeVO> convertPage(IPage<ErrorCodeDO> page);

    ErrorCodeDO convert (ErrorCodeCreateDTO bean);

    ErrorCodeDO convert (ErrorCodeUpdateDTO bean);

    ErrorCodeDO convert(ErrorCodeAutoGenerateReqDTO bean);

    List<ErrorCodeRespDTO> convertList02(List<ErrorCodeDO> list);

}
