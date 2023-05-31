package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.Saleslead;
import org.springframework.data.jpa.repository.*;

/**
* SalesleadRepository
*
*/
public interface SalesleadRepository extends JpaRepository<Saleslead, Long>, JpaSpecificationExecutor<Saleslead> {

}
