package cn.iocoder.yudao.module.jl.repository;

import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Customer;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long> {
}