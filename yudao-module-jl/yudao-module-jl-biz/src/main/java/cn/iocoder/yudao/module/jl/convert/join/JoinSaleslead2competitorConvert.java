package cn.iocoder.yudao.module.jl.convert.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;

/**
 * 销售线索中竞争对手的报价 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2competitorConvert {

    JoinSaleslead2competitorConvert INSTANCE = Mappers.getMapper(JoinSaleslead2competitorConvert.class);

    JoinSaleslead2competitorDO convert(JoinSaleslead2competitorCreateReqVO bean);

    JoinSaleslead2competitorDO convert(JoinSaleslead2competitorUpdateReqVO bean);

    JoinSaleslead2competitorRespVO convert(JoinSaleslead2competitorDO bean);

    List<JoinSaleslead2competitorRespVO> convertList(List<JoinSaleslead2competitorDO> list);

    PageResult<JoinSaleslead2competitorRespVO> convertPage(PageResult<JoinSaleslead2competitorDO> page);

    List<JoinSaleslead2competitorExcelVO> convertList02(List<JoinSaleslead2competitorDO> list);

}
