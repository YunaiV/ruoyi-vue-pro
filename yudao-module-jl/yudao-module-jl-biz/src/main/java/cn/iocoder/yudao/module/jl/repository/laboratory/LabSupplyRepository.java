package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.LabSupply;
import org.springframework.data.jpa.repository.*;

/**
* LabSupplyRepository
*
*/
public interface LabSupplyRepository extends JpaRepository<LabSupply, Long>, JpaSpecificationExecutor<LabSupply> {

}
