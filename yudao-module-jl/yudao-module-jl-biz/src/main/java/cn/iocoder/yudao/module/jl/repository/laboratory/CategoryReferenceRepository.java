package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryReference;
import org.springframework.data.jpa.repository.*;

/**
* CategoryReferenceRepository
*
*/
public interface CategoryReferenceRepository extends JpaRepository<CategoryReference, Long>, JpaSpecificationExecutor<CategoryReference> {

}
