package cn.iocoder.yudao.module.system.convert.dict;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.data.*;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface DictDataConvert {

    DictDataConvert INSTANCE = Mappers.getMapper(DictDataConvert.class);

    List<DictDataSimpleRespVO> convertList(List<DictDataDO> list);

    DictDataRespVO convert(DictDataDO bean);

    PageResult<DictDataRespVO> convertPage(PageResult<DictDataDO> page);

    DictDataDO convert(DictDataUpdateReqVO bean);

    DictDataDO convert(DictDataCreateReqVO bean);

    List<DictDataExcelVO> convertList02(List<DictDataDO> bean);

    DictDataRespDTO convert02(DictDataDO bean);

}
