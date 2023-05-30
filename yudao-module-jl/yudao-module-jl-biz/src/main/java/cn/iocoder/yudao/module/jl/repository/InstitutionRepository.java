package cn.iocoder.yudao.module.jl.repository;

import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Institution;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface InstitutionRepository extends PagingAndSortingRepository<Institution, Long>, JpaSpecificationExecutor<Institution> {
}