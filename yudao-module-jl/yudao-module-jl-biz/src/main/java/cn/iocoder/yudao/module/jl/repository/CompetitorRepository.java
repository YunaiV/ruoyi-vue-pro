package cn.iocoder.yudao.module.jl.repository;

import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Competitor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CompetitorRepository extends PagingAndSortingRepository<Competitor, Long> {
}