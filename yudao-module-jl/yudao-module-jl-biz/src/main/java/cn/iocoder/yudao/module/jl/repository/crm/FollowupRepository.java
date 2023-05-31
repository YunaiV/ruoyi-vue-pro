package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.Followup;
import org.springframework.data.jpa.repository.*;

/**
* FollowupRepository
*
*/
public interface FollowupRepository extends JpaRepository<Followup, Long>, JpaSpecificationExecutor<Followup> {

}
