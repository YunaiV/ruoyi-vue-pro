package cn.iocoder.yudao.module.system.convert.dept;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.post.*;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.SysPostDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PostConvert {

    PostConvert INSTANCE = Mappers.getMapper(PostConvert.class);

    List<PostSimpleRespVO> convertList02(List<SysPostDO> list);

    PageResult<PostRespVO> convertPage(PageResult<SysPostDO> page);

    PostRespVO convert(SysPostDO id);

    SysPostDO convert(PostCreateReqVO bean);

    SysPostDO convert(PostUpdateReqVO reqVO);

    List<PostExcelVO> convertList03(List<SysPostDO> list);

}
