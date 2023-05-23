package cn.iocoder.yudao.module.jl.convert.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinCustomer2saleDO;

/**
 * 客户所属的销售人员 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinCustomer2saleConvert {

    JoinCustomer2saleConvert INSTANCE = Mappers.getMapper(JoinCustomer2saleConvert.class);

    JoinCustomer2saleDO convert(JoinCustomer2saleCreateReqVO bean);

    JoinCustomer2saleDO convert(JoinCustomer2saleUpdateReqVO bean);

    JoinCustomer2saleRespVO convert(JoinCustomer2saleDO bean);

    List<JoinCustomer2saleRespVO> convertList(List<JoinCustomer2saleDO> list);

    PageResult<JoinCustomer2saleRespVO> convertPage(PageResult<JoinCustomer2saleDO> page);

    List<JoinCustomer2saleExcelVO> convertList02(List<JoinCustomer2saleDO> list);

}
