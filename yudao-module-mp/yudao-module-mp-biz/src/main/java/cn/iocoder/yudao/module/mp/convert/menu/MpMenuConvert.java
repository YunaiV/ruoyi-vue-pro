package cn.iocoder.yudao.module.mp.convert.menu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;

@Mapper
public interface MpMenuConvert {

    MpMenuConvert INSTANCE = Mappers.getMapper(MpMenuConvert.class);

    MpMenuDO convert(MpMenuSaveReqVO bean);

    MpMenuRespVO convert(MpMenuDO bean);

}
