package cn.iocoder.yudao.userserver.modules.system.convert.dict;

import cn.iocoder.yudao.framework.dict.core.dto.DictDataRespDTO;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.dict.SysDictDataDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SysDictDataConvert {

    SysDictDataConvert INSTANCE = Mappers.getMapper(SysDictDataConvert.class);

    DictDataRespDTO convert02(SysDictDataDO bean);

    List<DictDataRespDTO> convertList03(Collection<SysDictDataDO> list);

}
