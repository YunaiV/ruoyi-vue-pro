package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.SalesleadManager;
import org.springframework.data.jpa.repository.*;

/**
* SalesleadManagerRepository
*
*/
public interface SalesleadManagerRepository extends JpaRepository<SalesleadManager, Long>, JpaSpecificationExecutor<SalesleadManager> {

}
