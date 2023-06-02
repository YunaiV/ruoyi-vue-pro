package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectCategory;
import org.springframework.data.jpa.repository.*;

/**
* ProjectCategoryRepository
*
*/
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long>, JpaSpecificationExecutor<ProjectCategory> {

}
