package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCustomerPlan;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

/**
* SalesleadCustomerPlanRepository
*
*/
public interface SalesleadCustomerPlanRepository extends JpaRepository<SalesleadCustomerPlan, Long>, JpaSpecificationExecutor<SalesleadCustomerPlan> {
    @Transactional
    @Modifying
    @Query("delete from SalesleadCustomerPlan s where s.salesleadId = ?1")
    void deleteBySalesleadId(Long salesleadId);

}
