package cn.iocoder.yudao.module.jl.repository.crm;

import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCustomerPlan;
import org.springframework.data.jpa.repository.*;

/**
* SalesleadCustomerPlanRepository
*
*/
public interface SalesleadCustomerPlanRepository extends JpaRepository<SalesleadCustomerPlan, Long>, JpaSpecificationExecutor<SalesleadCustomerPlan> {

}
