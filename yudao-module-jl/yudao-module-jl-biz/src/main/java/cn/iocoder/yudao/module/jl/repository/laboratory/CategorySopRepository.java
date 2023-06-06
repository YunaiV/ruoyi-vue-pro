package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySop;
import org.springframework.data.jpa.repository.*;

import javax.transaction.Transactional;

/**
* CategorySopRepository
*
*/
public interface CategorySopRepository extends JpaRepository<CategorySop, Long>, JpaSpecificationExecutor<CategorySop> {
    @Modifying
    @Transactional
    @Query("delete from CategorySop sop where sop.categoryId = ?1")
    void deleteByCategoryId(Long categoryId);

}
