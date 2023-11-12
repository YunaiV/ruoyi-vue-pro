package cn.iocoder.yudao.module.infra.convert.demo01;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.infra.controller.admin.demo01.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo01.InfraDemo01StudentDO;

/**
 * 学生 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo01StudentConvert {

    InfraDemo01StudentConvert INSTANCE = Mappers.getMapper(InfraDemo01StudentConvert.class);

    InfraDemo01StudentDO convert(InfraDemo01StudentCreateReqVO bean);

    InfraDemo01StudentDO convert(InfraDemo01StudentUpdateReqVO bean);

    InfraDemo01StudentRespVO convert(InfraDemo01StudentDO bean);

    List<InfraDemo01StudentRespVO> convertList(List<InfraDemo01StudentDO> list);

    PageResult<InfraDemo01StudentRespVO> convertPage(PageResult<InfraDemo01StudentDO> page);

    List<InfraDemo01StudentExcelVO> convertList02(List<InfraDemo01StudentDO> list);

}