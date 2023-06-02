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
import cn.iocoder.yudao.module.jl.entity.project.ProjectCategory;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.project.ProjectCategoryMapper;
import cn.iocoder.yudao.module.jl.repository.project.ProjectCategoryRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 项目的实验名目 Service 实现类
 *
 */
@Service
@Validated
public class ProjectCategoryServiceImpl implements ProjectCategoryService {

    @Resource
    private ProjectCategoryRepository projectCategoryRepository;

    @Resource
    private ProjectCategoryMapper projectCategoryMapper;

    @Override
    public Long createProjectCategory(ProjectCategoryCreateReqVO createReqVO) {
        // 插入
        ProjectCategory projectCategory = projectCategoryMapper.toEntity(createReqVO);
        projectCategoryRepository.save(projectCategory);
        // 返回
        return projectCategory.getId();
    }

    @Override
    public void updateProjectCategory(ProjectCategoryUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectCategoryExists(updateReqVO.getId());
        // 更新
        ProjectCategory updateObj = projectCategoryMapper.toEntity(updateReqVO);
        projectCategoryRepository.save(updateObj);
    }

    @Override
    public void deleteProjectCategory(Long id) {
        // 校验存在
        validateProjectCategoryExists(id);
        // 删除
        projectCategoryRepository.deleteById(id);
    }

    private void validateProjectCategoryExists(Long id) {
        projectCategoryRepository.findById(id).orElseThrow(() -> exception(PROJECT_CATEGORY_NOT_EXISTS));
    }

    @Override
    public Optional<ProjectCategory> getProjectCategory(Long id) {
        return projectCategoryRepository.findById(id);
    }

    @Override
    public List<ProjectCategory> getProjectCategoryList(Collection<Long> ids) {
        return StreamSupport.stream(projectCategoryRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProjectCategory> getProjectCategoryPage(ProjectCategoryPageReqVO pageReqVO, ProjectCategoryPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<ProjectCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getQuoteId() != null) {
                predicates.add(cb.equal(root.get("quoteId"), pageReqVO.getQuoteId()));
            }

            if(pageReqVO.getScheduleId() != null) {
                predicates.add(cb.equal(root.get("scheduleId"), pageReqVO.getScheduleId()));
            }

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }

            if(pageReqVO.getCategoryType() != null) {
                predicates.add(cb.equal(root.get("categoryType"), pageReqVO.getCategoryType()));
            }

            if(pageReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), pageReqVO.getCategoryId()));
            }

            if(pageReqVO.getOperatorId() != null) {
                predicates.add(cb.equal(root.get("operatorId"), pageReqVO.getOperatorId()));
            }

            if(pageReqVO.getDemand() != null) {
                predicates.add(cb.equal(root.get("demand"), pageReqVO.getDemand()));
            }

            if(pageReqVO.getInterference() != null) {
                predicates.add(cb.equal(root.get("interference"), pageReqVO.getInterference()));
            }

            if(pageReqVO.getDependIds() != null) {
                predicates.add(cb.equal(root.get("dependIds"), pageReqVO.getDependIds()));
            }

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ProjectCategory> page = projectCategoryRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<ProjectCategory> getProjectCategoryList(ProjectCategoryExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<ProjectCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getQuoteId() != null) {
                predicates.add(cb.equal(root.get("quoteId"), exportReqVO.getQuoteId()));
            }

            if(exportReqVO.getScheduleId() != null) {
                predicates.add(cb.equal(root.get("scheduleId"), exportReqVO.getScheduleId()));
            }

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }

            if(exportReqVO.getCategoryType() != null) {
                predicates.add(cb.equal(root.get("categoryType"), exportReqVO.getCategoryType()));
            }

            if(exportReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), exportReqVO.getCategoryId()));
            }

            if(exportReqVO.getOperatorId() != null) {
                predicates.add(cb.equal(root.get("operatorId"), exportReqVO.getOperatorId()));
            }

            if(exportReqVO.getDemand() != null) {
                predicates.add(cb.equal(root.get("demand"), exportReqVO.getDemand()));
            }

            if(exportReqVO.getInterference() != null) {
                predicates.add(cb.equal(root.get("interference"), exportReqVO.getInterference()));
            }

            if(exportReqVO.getDependIds() != null) {
                predicates.add(cb.equal(root.get("dependIds"), exportReqVO.getDependIds()));
            }

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return projectCategoryRepository.findAll(spec);
    }

    private Sort createSort(ProjectCategoryPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getQuoteId() != null) {
            orders.add(new Sort.Order(order.getQuoteId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "quoteId"));
        }

        if (order.getScheduleId() != null) {
            orders.add(new Sort.Order(order.getScheduleId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "scheduleId"));
        }

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }

        if (order.getCategoryType() != null) {
            orders.add(new Sort.Order(order.getCategoryType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "categoryType"));
        }

        if (order.getCategoryId() != null) {
            orders.add(new Sort.Order(order.getCategoryId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "categoryId"));
        }

        if (order.getOperatorId() != null) {
            orders.add(new Sort.Order(order.getOperatorId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "operatorId"));
        }

        if (order.getDemand() != null) {
            orders.add(new Sort.Order(order.getDemand().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "demand"));
        }

        if (order.getInterference() != null) {
            orders.add(new Sort.Order(order.getInterference().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "interference"));
        }

        if (order.getDependIds() != null) {
            orders.add(new Sort.Order(order.getDependIds().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "dependIds"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}