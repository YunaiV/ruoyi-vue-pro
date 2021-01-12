package cn.iocoder.dashboard.modules.system.convert.dict;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictTypeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysDictTypeConvert {

    SysDictTypeConvert INSTANCE = Mappers.getMapper(SysDictTypeConvert.class);

    PageResult<SysDictTypeRespVO> convertPage(PageResult<SysDictTypeDO> bean);

    SysDictTypeRespVO convert(SysDictTypeDO bean);

}
