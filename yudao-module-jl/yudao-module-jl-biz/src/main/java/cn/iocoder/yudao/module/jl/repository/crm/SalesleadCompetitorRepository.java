package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

/**
* SalesleadCompetitorRepository
*
*/
public interface SalesleadCompetitorRepository extends JpaRepository<SalesleadCompetitor, Long>, JpaSpecificationExecutor<SalesleadCompetitor> {
    @Transactional
    @Modifying
    @Query("delete from SalesleadCompetitor s where s.salesleadId = ?1")
    void deleteBySalesleadId(Long salesleadId);

}
