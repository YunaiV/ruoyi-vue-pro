package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.Saleslead;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

/**
* SalesleadRepository
*
*/
public interface SalesleadRepository extends JpaRepository<Saleslead, Long>, JpaSpecificationExecutor<Saleslead> {
    @Transactional
    @Modifying
    @Query("update Saleslead s set s.quotation = ?2 where s.id = ?1")
    void updateQuotationById(Long id, Long quotationId);

}
