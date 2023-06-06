package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.ChargeItem;
import org.springframework.data.jpa.repository.*;

/**
* ChargeItemRepository
*
*/
public interface ChargeItemRepository extends JpaRepository<ChargeItem, Long>, JpaSpecificationExecutor<ChargeItem> {

}
