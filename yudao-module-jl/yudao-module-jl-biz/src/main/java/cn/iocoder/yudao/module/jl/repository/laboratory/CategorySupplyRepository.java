package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySupply;
import org.springframework.data.jpa.repository.*;

import javax.transaction.Transactional;

/**
* CategorySupplyRepository
*
*/
public interface CategorySupplyRepository extends JpaRepository<CategorySupply, Long>, JpaSpecificationExecutor<CategorySupply> {

    @Modifying
    @Transactional
    @Query("delete from CategorySupply supply where supply.categoryId = ?1")
    void deleteByCategoryId(Long categoryId);
}
