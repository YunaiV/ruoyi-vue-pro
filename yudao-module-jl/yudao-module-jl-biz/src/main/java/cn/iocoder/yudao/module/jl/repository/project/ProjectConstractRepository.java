package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectConstract;
import org.springframework.data.jpa.repository.*;

/**
* ProjectConstractRepository
*
*/
public interface ProjectConstractRepository extends JpaRepository<ProjectConstract, Long>, JpaSpecificationExecutor<ProjectConstract> {

}
