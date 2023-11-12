package cn.iocoder.yudao.module.infra.convert.demo11;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.infra.controller.admin.demo11.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentDO;

/**
 * 学生 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo11StudentConvert {

    InfraDemo11StudentConvert INSTANCE = Mappers.getMapper(InfraDemo11StudentConvert.class);

    InfraDemo11StudentDO convert(InfraDemo11StudentCreateReqVO bean);

    InfraDemo11StudentDO convert(InfraDemo11StudentUpdateReqVO bean);

    InfraDemo11StudentRespVO convert(InfraDemo11StudentDO bean);

    List<InfraDemo11StudentRespVO> convertList(List<InfraDemo11StudentDO> list);

    PageResult<InfraDemo11StudentRespVO> convertPage(PageResult<InfraDemo11StudentDO> page);

    List<InfraDemo11StudentExcelVO> convertList02(List<InfraDemo11StudentDO> list);

}