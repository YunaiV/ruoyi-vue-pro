package cn.iocoder.yudao.module.jl.service.crm;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Competitor;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.CompetitorMapper;
import cn.iocoder.yudao.module.jl.repository.crm.CompetitorRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 友商 Service 实现类
 *
 */
@Service
@Validated
public class CompetitorServiceImpl implements CompetitorService {

    @Resource
    private CompetitorRepository competitorRepository;

    @Resource
    private CompetitorMapper competitorMapper;

    @Override
    public Long createCompetitor(CompetitorCreateReqVO createReqVO) {
        // 插入
        Competitor competitor = competitorMapper.toEntity(createReqVO);
        competitorRepository.save(competitor);
        // 返回
        return competitor.getId();
    }

    @Override
    public void updateCompetitor(CompetitorUpdateReqVO updateReqVO) {
        // 校验存在
        validateCompetitorExists(updateReqVO.getId());
        // 更新
        Competitor updateObj = competitorMapper.toEntity(updateReqVO);
        competitorRepository.save(updateObj);
    }

    @Override
    public void deleteCompetitor(Long id) {
        // 校验存在
        validateCompetitorExists(id);
        // 删除
        competitorRepository.deleteById(id);
    }

    private void validateCompetitorExists(Long id) {
        competitorRepository.findById(id).orElseThrow(() -> exception(COMPETITOR_NOT_EXISTS));
    }

    @Override
    public Optional<Competitor> getCompetitor(Long id) {
        return competitorRepository.findById(id);
    }

    @Override
    public List<Competitor> getCompetitorList(Collection<Long> ids) {
        return StreamSupport.stream(competitorRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Competitor> getCompetitorPage(CompetitorPageReqVO pageReqVO, CompetitorPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<Competitor> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getContactName() != null) {
                predicates.add(cb.like(root.get("contactName"), "%" + pageReqVO.getContactName() + "%"));
            }

            if(pageReqVO.getPhone() != null) {
                predicates.add(cb.like(root.get("phone"), "%" + pageReqVO.getPhone() + "%"));
            }

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }

            if(pageReqVO.getAdvantage() != null) {
                predicates.add(cb.equal(root.get("advantage"), pageReqVO.getAdvantage()));
            }

            if(pageReqVO.getDisadvantage() != null) {
                predicates.add(cb.equal(root.get("disadvantage"), pageReqVO.getDisadvantage()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<Competitor> page = competitorRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<Competitor> getCompetitorList(CompetitorExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<Competitor> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getContactName() != null) {
                predicates.add(cb.like(root.get("contactName"), "%" + exportReqVO.getContactName() + "%"));
            }

            if(exportReqVO.getPhone() != null) {
                predicates.add(cb.like(root.get("phone"), "%" + exportReqVO.getPhone() + "%"));
            }

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }

            if(exportReqVO.getAdvantage() != null) {
                predicates.add(cb.equal(root.get("advantage"), exportReqVO.getAdvantage()));
            }

            if(exportReqVO.getDisadvantage() != null) {
                predicates.add(cb.equal(root.get("disadvantage"), exportReqVO.getDisadvantage()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return competitorRepository.findAll(spec);
    }

    private Sort createSort(CompetitorPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getContactName() != null) {
            orders.add(new Sort.Order(order.getContactName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "contactName"));
        }

        if (order.getPhone() != null) {
            orders.add(new Sort.Order(order.getPhone().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "phone"));
        }

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }

        if (order.getAdvantage() != null) {
            orders.add(new Sort.Order(order.getAdvantage().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "advantage"));
        }

        if (order.getDisadvantage() != null) {
            orders.add(new Sort.Order(order.getDisadvantage().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "disadvantage"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}