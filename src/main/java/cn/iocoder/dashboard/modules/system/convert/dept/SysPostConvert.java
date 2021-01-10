package cn.iocoder.dashboard.modules.system.convert.dept;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.post.SysPostSimpleRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysPostDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysPostConvert {

    SysPostConvert INSTANCE = Mappers.getMapper(SysPostConvert.class);

    List<SysPostSimpleRespVO> convertList02(List<SysPostDO> list);

}
