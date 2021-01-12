package cn.iocoder.dashboard.modules.system.convert.dict;

import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDataDictSimpleVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysDictDataConvert {

    SysDictDataConvert INSTANCE = Mappers.getMapper(SysDictDataConvert.class);

    List<SysDataDictSimpleVO> convertList(List<SysDictDataDO> list);

}
