package cn.iocoder.yudao.module.system.convert.dept;

import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptRespVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysDeptDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DeptConvert {

    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);

    List<DeptRespVO> convertList(List<SysDeptDO> list);

    List<DeptSimpleRespVO> convertList02(List<SysDeptDO> list);

    DeptRespVO convert(SysDeptDO bean);

    SysDeptDO convert(DeptCreateReqVO bean);

    SysDeptDO convert(DeptUpdateReqVO bean);

}
