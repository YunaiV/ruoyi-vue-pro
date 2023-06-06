package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import org.springframework.data.jpa.repository.*;

/**
* ProjectQuoteRepository
*
*/
public interface ProjectQuoteRepository extends JpaRepository<ProjectQuote, Long>, JpaSpecificationExecutor<ProjectQuote> {

}
