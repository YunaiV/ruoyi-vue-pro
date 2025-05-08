package cn.iocoder.yudao.module.system.convert.dept;

import cn.iocoder.yudao.module.system.api.dept.dto.DeptReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptSaveReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.*;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


import java.util.List;

/**
 * @author Administrator
 */
@Mapper
public interface DeptConvert {

    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);

    @Mapping(target = "children", ignore = true)
    List<DeptTreeRespVO> buildDeptTree(List<DeptDO> list);

    DeptRespDTO toRespDTO(DeptDO dept);

    List<DeptRespDTO> toRespDTOs(List<DeptDO> depts);

    DeptReqDTO toReqDTO(DeptListReqVO reqVO);

    DeptSaveReqDTO toSaveReqDTO(DeptSaveReqVO dept);

    DeptRespVO toRespVO(DeptRespDTO dept);

    List<DeptRespVO> toRespVOs(List<DeptRespDTO> depts);

    List<DeptSimpleRespVO> toSimpleRespVOs(List<DeptRespDTO> depts);
}
