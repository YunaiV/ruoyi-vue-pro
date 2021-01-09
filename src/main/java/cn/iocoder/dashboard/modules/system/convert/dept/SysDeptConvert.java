package cn.iocoder.dashboard.modules.system.convert.dept;

import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptRespVO;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptSimpleRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysDeptConvert {

    SysDeptConvert INSTANCE = Mappers.getMapper(SysDeptConvert.class);

    List<SysDeptRespVO> convertList(List<SysDeptDO> list);

    List<SysDeptSimpleRespVO> convertList02(List<SysDeptDO> list);

}
