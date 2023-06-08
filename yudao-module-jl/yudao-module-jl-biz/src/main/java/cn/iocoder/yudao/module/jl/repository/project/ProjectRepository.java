package cn.iocoder.yudao.module.jl.repository.project;

import cn.iocoder.yudao.module.jl.entity.project.Project;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

/**
* ProjectRepository
*
*/
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    @Transactional
    @Modifying
    @Query("update Project p set p.currentScheduleId = ?2 where p.id = ?1")
    int updateCurrentScheduleIdById(Long projectId, Long scheduleId);

}
