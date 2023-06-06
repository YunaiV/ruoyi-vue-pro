package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import org.springframework.data.jpa.repository.*;

/**
* SalesleadCompetitorRepository
*
*/
public interface SalesleadCompetitorRepository extends JpaRepository<SalesleadCompetitor, Long>, JpaSpecificationExecutor<SalesleadCompetitor> {

}
