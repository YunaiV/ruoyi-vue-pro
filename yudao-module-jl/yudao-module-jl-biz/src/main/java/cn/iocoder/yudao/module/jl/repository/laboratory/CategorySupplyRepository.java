package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySupply;
import org.springframework.data.jpa.repository.*;

/**
* CategorySupplyRepository
*
*/
public interface CategorySupplyRepository extends JpaRepository<CategorySupply, Long>, JpaSpecificationExecutor<CategorySupply> {

}
