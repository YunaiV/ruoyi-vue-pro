package cn.iocoder.yudao.module.system.convert.dict;

import cn.iocoder.yudao.module.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.yudao.framework.dict.core.dto.DictDataRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SysDictDataCoreConvert {

    SysDictDataCoreConvert INSTANCE = Mappers.getMapper(SysDictDataCoreConvert.class);

    DictDataRespDTO convert02(SysDictDataDO bean);

    List<DictDataRespDTO> convertList03(Collection<SysDictDataDO> list);

}
