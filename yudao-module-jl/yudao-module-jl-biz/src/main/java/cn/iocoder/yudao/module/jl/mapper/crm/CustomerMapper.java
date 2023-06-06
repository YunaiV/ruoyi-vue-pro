package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Customer;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {
    Customer toEntity(CustomerCreateReqVO dto);

    Customer toEntity(CustomerUpdateReqVO dto);

    CustomerRespVO toDto(Customer entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Customer partialUpdate(CustomerUpdateReqVO dto, @MappingTarget Customer entity);

    List<CustomerExcelVO> toExcelList(List<Customer> list);

    PageResult<CustomerRespVO> toPage(PageResult<Customer> page);
}