package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySop;
import org.springframework.data.jpa.repository.*;

/**
* CategorySopRepository
*
*/
public interface CategorySopRepository extends JpaRepository<CategorySop, Long>, JpaSpecificationExecutor<CategorySop> {
    void deleteByCategoryId(Long categoryId);

}
