package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.Competitor;
import org.springframework.data.jpa.repository.*;

/**
* CompetitorRepository
*
*/
public interface CompetitorRepository extends JpaRepository<Competitor, Long>, JpaSpecificationExecutor<Competitor> {

}
