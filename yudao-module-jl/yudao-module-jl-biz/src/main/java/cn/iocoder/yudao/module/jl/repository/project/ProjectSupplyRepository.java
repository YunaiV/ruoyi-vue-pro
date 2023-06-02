package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectSupply;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
* ProjectSupplyRepository
*
*/
public interface ProjectSupplyRepository extends JpaRepository<ProjectSupply, Long>, JpaSpecificationExecutor<ProjectSupply> {
    @Transactional
    @Modifying
    @Query("delete from ProjectSupply p where p.projectCategoryId in ?1")
    void deleteByProjectCategoryIdIn(Collection<Long> projectCategoryIds);

}
