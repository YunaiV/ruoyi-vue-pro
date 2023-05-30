package cn.iocoder.yudao.module.jl.mapper;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.CustomerCreateReq;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Customer;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {
    Customer toEntity(CustomerDto customerDto);

    CustomerDto toDto(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Customer partialUpdate(CustomerDto customerDto, @MappingTarget Customer customer);

    @Mapping(source = "universityId", target = "university.id")
    @Mapping(source = "hospitalId", target = "hospital.id")
    @Mapping(source = "companyId", target = "company.id")
    @Mapping(source = "salesId", target = "sales.id")
    Customer toEntity(CustomerCreateReq customerCreateReq);

    @InheritInverseConfiguration(name = "toEntity")
    CustomerCreateReq toDto1(Customer customer);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Customer partialUpdate(CustomerCreateReq customerCreateReq, @MappingTarget Customer customer);
}