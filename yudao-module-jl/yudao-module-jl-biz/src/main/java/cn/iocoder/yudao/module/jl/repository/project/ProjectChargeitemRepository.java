package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectChargeitem;
import org.springframework.data.jpa.repository.*;

/**
* ProjectChargeitemRepository
*
*/
public interface ProjectChargeitemRepository extends JpaRepository<ProjectChargeitem, Long>, JpaSpecificationExecutor<ProjectChargeitem> {

}
