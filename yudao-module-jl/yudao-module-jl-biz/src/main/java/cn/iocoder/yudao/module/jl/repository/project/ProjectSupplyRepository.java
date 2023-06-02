package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectSupply;
import org.springframework.data.jpa.repository.*;

/**
* ProjectSupplyRepository
*
*/
public interface ProjectSupplyRepository extends JpaRepository<ProjectSupply, Long>, JpaSpecificationExecutor<ProjectSupply> {

}
