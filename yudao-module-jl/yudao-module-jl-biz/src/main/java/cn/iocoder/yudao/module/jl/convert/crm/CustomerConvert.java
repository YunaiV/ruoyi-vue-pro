package cn.iocoder.yudao.module.jl.convert.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDO;

/**
 * 客户 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface CustomerConvert {

    CustomerConvert INSTANCE = Mappers.getMapper(CustomerConvert.class);

    CustomerDO convert(CustomerCreateReqVO bean);

    CustomerDO convert(CustomerUpdateReqVO bean);

    CustomerDO convert(CustomerUpdateFollowupVO bean);

    CustomerDO convert(CustomerUpdateSalesLeadVO bean);

    CustomerRespVO convert(CustomerDO bean);

    List<CustomerRespVO> convertList(List<CustomerDO> list);

    PageResult<CustomerRespVO> convertPage(PageResult<CustomerDO> page);

    List<CustomerExcelVO> convertList02(List<CustomerDO> list);

}
