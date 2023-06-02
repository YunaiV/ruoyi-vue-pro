package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectCategory;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
* ProjectCategoryRepository
*
*/
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long>, JpaSpecificationExecutor<ProjectCategory> {

    @Modifying
    @Query(value = "delete from ProjectCategory category where category.quoteId = ?1")
    void deleteByQuoteId(Long quoteId);

    @Modifying
    @Query("SELECT e FROM ProjectCategory e WHERE e.quoteId = :quoteId")
    List<ProjectCategory> getByQuoteId(Long quoteId);
}
