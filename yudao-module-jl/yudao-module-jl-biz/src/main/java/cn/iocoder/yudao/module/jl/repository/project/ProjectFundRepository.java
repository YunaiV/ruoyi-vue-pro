package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectFund;
import org.springframework.data.jpa.repository.*;

/**
* ProjectFundRepository
*
*/
public interface ProjectFundRepository extends JpaRepository<ProjectFund, Long>, JpaSpecificationExecutor<ProjectFund> {

}
