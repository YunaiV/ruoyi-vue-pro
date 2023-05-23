package cn.iocoder.yudao.module.jl.convert.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;

/**
 * 销售线索中的客户方案 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinSaleslead2customerplanConvert {

    JoinSaleslead2customerplanConvert INSTANCE = Mappers.getMapper(JoinSaleslead2customerplanConvert.class);

    JoinSaleslead2customerplanDO convert(JoinSaleslead2customerplanCreateReqVO bean);

    JoinSaleslead2customerplanDO convert(JoinSaleslead2customerplanUpdateReqVO bean);

    JoinSaleslead2customerplanRespVO convert(JoinSaleslead2customerplanDO bean);

    List<JoinSaleslead2customerplanRespVO> convertList(List<JoinSaleslead2customerplanDO> list);

    PageResult<JoinSaleslead2customerplanRespVO> convertPage(PageResult<JoinSaleslead2customerplanDO> page);

    List<JoinSaleslead2customerplanExcelVO> convertList02(List<JoinSaleslead2customerplanDO> list);

}
