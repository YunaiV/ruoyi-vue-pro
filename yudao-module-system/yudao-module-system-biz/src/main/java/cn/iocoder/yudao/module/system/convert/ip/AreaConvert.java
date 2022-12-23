package cn.iocoder.yudao.module.system.convert.ip;

import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AreaConvert {

    AreaConvert INSTANCE = Mappers.getMapper(AreaConvert.class);

    List<AreaNodeRespVO> convertList(List<Area> list);

}
