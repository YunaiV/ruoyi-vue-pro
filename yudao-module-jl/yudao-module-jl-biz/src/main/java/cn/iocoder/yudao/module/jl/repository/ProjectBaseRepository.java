package cn.iocoder.yudao.module.jl.repository;

import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProjectBaseRepository extends PagingAndSortingRepository<ProjectBaseDO, Long> {
}
