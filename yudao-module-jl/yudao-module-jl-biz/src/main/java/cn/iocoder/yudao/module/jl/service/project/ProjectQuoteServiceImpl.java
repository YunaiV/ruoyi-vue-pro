package cn.iocoder.yudao.module.jl.service.project;

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
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.project.ProjectQuoteMapper;
import cn.iocoder.yudao.module.jl.repository.project.ProjectQuoteRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 项目报价 Service 实现类
 *
 */
@Service
@Validated
public class ProjectQuoteServiceImpl implements ProjectQuoteService {

    @Resource
    private ProjectQuoteRepository projectQuoteRepository;

    @Resource
    private ProjectQuoteMapper projectQuoteMapper;

    @Override
    public Long createProjectQuote(ProjectQuoteCreateReqVO createReqVO) {
        // 插入
        ProjectQuote projectQuote = projectQuoteMapper.toEntity(createReqVO);
        projectQuoteRepository.save(projectQuote);
        // 返回
        return projectQuote.getId();
    }

    @Override
    public void updateProjectQuote(ProjectQuoteUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectQuoteExists(updateReqVO.getId());
        // 更新
        ProjectQuote updateObj = projectQuoteMapper.toEntity(updateReqVO);
        projectQuoteRepository.save(updateObj);
    }

    @Override
    public void deleteProjectQuote(Long id) {
        // 校验存在
        validateProjectQuoteExists(id);
        // 删除
        projectQuoteRepository.deleteById(id);
    }

    private void validateProjectQuoteExists(Long id) {
        projectQuoteRepository.findById(id).orElseThrow(() -> exception(PROJECT_QUOTE_NOT_EXISTS));
    }

    @Override
    public Optional<ProjectQuote> getProjectQuote(Long id) {
        return projectQuoteRepository.findById(id);
    }

    @Override
    public List<ProjectQuote> getProjectQuoteList(Collection<Long> ids) {
        return StreamSupport.stream(projectQuoteRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProjectQuote> getProjectQuotePage(ProjectQuotePageReqVO pageReqVO, ProjectQuotePageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<ProjectQuote> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), pageReqVO.getSalesleadId()));
            }

            if(pageReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), pageReqVO.getProjectId()));
            }

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getReportUrl() != null) {
                predicates.add(cb.equal(root.get("reportUrl"), pageReqVO.getReportUrl()));
            }

            if(pageReqVO.getDiscount() != null) {
                predicates.add(cb.equal(root.get("discount"), pageReqVO.getDiscount()));
            }

            if(pageReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), pageReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ProjectQuote> page = projectQuoteRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<ProjectQuote> getProjectQuoteList(ProjectQuoteExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<ProjectQuote> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), exportReqVO.getSalesleadId()));
            }

            if(exportReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), exportReqVO.getProjectId()));
            }

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getReportUrl() != null) {
                predicates.add(cb.equal(root.get("reportUrl"), exportReqVO.getReportUrl()));
            }

            if(exportReqVO.getDiscount() != null) {
                predicates.add(cb.equal(root.get("discount"), exportReqVO.getDiscount()));
            }

            if(exportReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), exportReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return projectQuoteRepository.findAll(spec);
    }

    private Sort createSort(ProjectQuotePageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getSalesleadId() != null) {
            orders.add(new Sort.Order(order.getSalesleadId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "salesleadId"));
        }

        if (order.getProjectId() != null) {
            orders.add(new Sort.Order(order.getProjectId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "projectId"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getReportUrl() != null) {
            orders.add(new Sort.Order(order.getReportUrl().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "reportUrl"));
        }

        if (order.getDiscount() != null) {
            orders.add(new Sort.Order(order.getDiscount().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "discount"));
        }

        if (order.getStatus() != null) {
            orders.add(new Sort.Order(order.getStatus().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "status"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}