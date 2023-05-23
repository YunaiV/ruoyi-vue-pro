package cn.iocoder.yudao.module.jl.convert.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.SalesleadDO;

/**
 * 销售线索 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface SalesleadConvert {

    SalesleadConvert INSTANCE = Mappers.getMapper(SalesleadConvert.class);

    SalesleadDO convert(SalesleadCreateReqVO bean);

    SalesleadDO convert(SalesleadUpdateReqVO bean);

    SalesleadRespVO convert(SalesleadDO bean);

    List<SalesleadRespVO> convertList(List<SalesleadDO> list);

    PageResult<SalesleadRespVO> convertPage(PageResult<SalesleadDO> page);

    List<SalesleadExcelVO> convertList02(List<SalesleadDO> list);

}
