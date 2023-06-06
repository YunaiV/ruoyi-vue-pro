package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectSop;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
* ProjectSopRepository
*
*/
public interface ProjectSopRepository extends JpaRepository<ProjectSop, Long>, JpaSpecificationExecutor<ProjectSop> {
    @Transactional
    @Modifying
    @Query("delete from ProjectSop p where p.projectCategoryId in ?1")
    void deleteByProjectCategoryIdIn(Collection<Long> projectCategoryIds);

}
