package cn.iocoder.yudao.module.jl.convert.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;

/**
 * 销售线索中的方案 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2reportConvert {

    JoinSaleslead2reportConvert INSTANCE = Mappers.getMapper(JoinSaleslead2reportConvert.class);

    JoinSaleslead2reportDO convert(JoinSaleslead2reportCreateReqVO bean);

    JoinSaleslead2reportDO convert(JoinSaleslead2reportUpdateReqVO bean);

    JoinSaleslead2reportRespVO convert(JoinSaleslead2reportDO bean);

    List<JoinSaleslead2reportRespVO> convertList(List<JoinSaleslead2reportDO> list);

    PageResult<JoinSaleslead2reportRespVO> convertPage(PageResult<JoinSaleslead2reportDO> page);

    List<JoinSaleslead2reportExcelVO> convertList02(List<JoinSaleslead2reportDO> list);

}
