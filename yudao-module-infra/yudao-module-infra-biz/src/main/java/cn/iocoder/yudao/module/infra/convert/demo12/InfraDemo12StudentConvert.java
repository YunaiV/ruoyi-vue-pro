package cn.iocoder.yudao.module.infra.convert.demo12;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.infra.controller.admin.demo12.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentDO;

/**
 * 学生 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo12StudentConvert {

    InfraDemo12StudentConvert INSTANCE = Mappers.getMapper(InfraDemo12StudentConvert.class);

    InfraDemo12StudentDO convert(InfraDemo12StudentCreateReqVO bean);

    InfraDemo12StudentDO convert(InfraDemo12StudentUpdateReqVO bean);

    InfraDemo12StudentRespVO convert(InfraDemo12StudentDO bean);

    List<InfraDemo12StudentRespVO> convertList(List<InfraDemo12StudentDO> list);

    PageResult<InfraDemo12StudentRespVO> convertPage(PageResult<InfraDemo12StudentDO> page);

    List<InfraDemo12StudentExcelVO> convertList02(List<InfraDemo12StudentDO> list);

}