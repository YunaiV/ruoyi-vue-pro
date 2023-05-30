package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
* InstitutionRepository
*
*/
public interface InstitutionRepository extends PagingAndSortingRepository<Institution, Long>, JpaSpecificationExecutor<Institution> {

}
