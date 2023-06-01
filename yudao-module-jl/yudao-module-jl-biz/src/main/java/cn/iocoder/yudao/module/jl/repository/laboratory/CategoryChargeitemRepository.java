package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryChargeitem;
import org.springframework.data.jpa.repository.*;

/**
* CategoryChargeitemRepository
*
*/
public interface CategoryChargeitemRepository extends JpaRepository<CategoryChargeitem, Long>, JpaSpecificationExecutor<CategoryChargeitem> {

    void deleteByCategoryId(Long categoryId);
}
