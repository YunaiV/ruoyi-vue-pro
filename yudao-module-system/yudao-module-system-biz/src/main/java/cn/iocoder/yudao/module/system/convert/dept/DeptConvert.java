package cn.iocoder.yudao.module.system.convert.dept;

import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptTreeRespVO;
import cn.iocoder.yudao.module.system.convert.oauth2.OAuth2OpenConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;


import java.util.List;

/**
 * @author Administrator
 */
@Mapper(componentModel = "spring")
public interface DeptConvert {

    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);

    @Mapping(target = "children", ignore = true)
    List<DeptTreeRespVO> convertList(List<DeptDO> list);
}
