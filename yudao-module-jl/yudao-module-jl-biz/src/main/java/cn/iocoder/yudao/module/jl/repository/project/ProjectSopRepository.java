package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectSop;
import org.springframework.data.jpa.repository.*;

/**
* ProjectSopRepository
*
*/
public interface ProjectSopRepository extends JpaRepository<ProjectSop, Long>, JpaSpecificationExecutor<ProjectSop> {

}
