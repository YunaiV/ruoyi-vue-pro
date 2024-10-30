package cn.iocoder.yudao.module.system.convert.dept;

import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptTreeRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


import java.util.List;

/**
 * @author Administrator
 */
@Mapper(componentModel = "spring", typeConversionPolicy = ReportingPolicy.ERROR)
public interface DeptConvert {

    @Mapping(target = "children", ignore = true)
    List<DeptTreeRespVO> convertList(List<DeptDO> list);
}
