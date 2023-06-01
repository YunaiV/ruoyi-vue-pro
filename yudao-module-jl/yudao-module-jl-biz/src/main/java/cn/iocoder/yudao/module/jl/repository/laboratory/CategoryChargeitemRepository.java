package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryChargeitem;
import org.springframework.data.jpa.repository.*;

import javax.transaction.Transactional;

/**
* CategoryChargeitemRepository
*
*/
public interface CategoryChargeitemRepository extends JpaRepository<CategoryChargeitem, Long>, JpaSpecificationExecutor<CategoryChargeitem> {

    @Modifying
    @Transactional
    @Query("delete from CategoryChargeitem item where item.categoryId = ?1")
    void deleteByCategoryId(Long categoryId);
}
