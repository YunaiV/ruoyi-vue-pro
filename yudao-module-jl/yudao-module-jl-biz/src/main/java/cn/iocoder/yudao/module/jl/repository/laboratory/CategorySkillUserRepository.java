package cn.iocoder.yudao.module.jl.repository.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySkillUser;
import org.springframework.data.jpa.repository.*;

/**
* CategorySkillUserRepository
*
*/
public interface CategorySkillUserRepository extends JpaRepository<CategorySkillUser, Long>, JpaSpecificationExecutor<CategorySkillUser> {

}
