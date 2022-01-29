package cn.iocoder.yudao.module.system.convert.dict;

import cn.iocoder.yudao.module.system.controller.admin.dict.vo.data.*;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.dict.core.dto.DictDataRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface DictDataConvert {

    DictDataConvert INSTANCE = Mappers.getMapper(DictDataConvert.class);

    List<DictDataSimpleRespVO> convertList(List<SysDictDataDO> list);

    DictDataRespVO convert(SysDictDataDO bean);

    PageResult<DictDataRespVO> convertPage(PageResult<SysDictDataDO> page);

    SysDictDataDO convert(DictDataUpdateReqVO bean);

    SysDictDataDO convert(DictDataCreateReqVO bean);

    List<DictDataExcelVO> convertList02(List<SysDictDataDO> bean);

    DictDataRespDTO convert02(SysDictDataDO bean);

    List<DictDataRespDTO> convertList03(Collection<SysDictDataDO> list);

}
