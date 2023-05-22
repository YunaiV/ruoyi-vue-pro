package cn.iocoder.yudao.module.jl.convert.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CompetitorDO;

/**
 * 友商 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface CompetitorConvert {

    CompetitorConvert INSTANCE = Mappers.getMapper(CompetitorConvert.class);

    CompetitorDO convert(CompetitorCreateReqVO bean);

    CompetitorDO convert(CompetitorUpdateReqVO bean);

    CompetitorRespVO convert(CompetitorDO bean);

    List<CompetitorRespVO> convertList(List<CompetitorDO> list);

    PageResult<CompetitorRespVO> convertPage(PageResult<CompetitorDO> page);

    List<CompetitorExcelVO> convertList02(List<CompetitorDO> list);

}
