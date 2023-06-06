package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectChargeitem;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
* ProjectChargeitemRepository
*
*/
public interface ProjectChargeitemRepository extends JpaRepository<ProjectChargeitem, Long>, JpaSpecificationExecutor<ProjectChargeitem> {
    @Transactional
    @Modifying
    @Query("delete from ProjectChargeitem p where p.projectCategoryId in ?1")
    int deleteByProjectCategoryIdIn(Collection<Long> projectCategoryIds);

}
