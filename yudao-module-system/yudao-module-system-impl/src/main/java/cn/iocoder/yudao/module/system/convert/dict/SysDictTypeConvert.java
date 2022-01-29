package cn.iocoder.yudao.module.system.convert.dict;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.dict.vo.type.*;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.SysDictTypeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysDictTypeConvert {

    SysDictTypeConvert INSTANCE = Mappers.getMapper(SysDictTypeConvert.class);

    PageResult<SysDictTypeRespVO> convertPage(PageResult<SysDictTypeDO> bean);

    SysDictTypeRespVO convert(SysDictTypeDO bean);

    SysDictTypeDO convert(SysDictTypeCreateReqVO bean);

    SysDictTypeDO convert(SysDictTypeUpdateReqVO bean);

    List<SysDictTypeSimpleRespVO> convertList(List<SysDictTypeDO> list);

    List<SysDictTypeExcelVO> convertList02(List<SysDictTypeDO> list);

}
