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
import cn.iocoder.yudao.module.jl.entity.project.ProjectFund;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.project.ProjectFundMapper;
import cn.iocoder.yudao.module.jl.repository.project.ProjectFundRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 项目款项 Service 实现类
 *
 */
@Service
@Validated
public class ProjectFundServiceImpl implements ProjectFundService {

    @Resource
    private ProjectFundRepository projectFundRepository;

    @Resource
    private ProjectFundMapper projectFundMapper;

    @Override
    public Long createProjectFund(ProjectFundCreateReqVO createReqVO) {
        // 插入
        ProjectFund projectFund = projectFundMapper.toEntity(createReqVO);
        projectFundRepository.save(projectFund);
        // 返回
        return projectFund.getId();
    }

    @Override
    public void updateProjectFund(ProjectFundUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectFundExists(updateReqVO.getId());
        // 更新
        ProjectFund updateObj = projectFundMapper.toEntity(updateReqVO);
        projectFundRepository.save(updateObj);
    }

    @Override
    public void deleteProjectFund(Long id) {
        // 校验存在
        validateProjectFundExists(id);
        // 删除
        projectFundRepository.deleteById(id);
    }

    private void validateProjectFundExists(Long id) {
        projectFundRepository.findById(id).orElseThrow(() -> exception(PROJECT_FUND_NOT_EXISTS));
    }

    @Override
    public Optional<ProjectFund> getProjectFund(Long id) {
        return projectFundRepository.findById(id);
    }

    @Override
    public List<ProjectFund> getProjectFundList(Collection<Long> ids) {
        return StreamSupport.stream(projectFundRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProjectFund> getProjectFundPage(ProjectFundPageReqVO pageReqVO, ProjectFundPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<ProjectFund> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getName() != null) {
                predicates.add(cb.equal(root.get("name"), pageReqVO.getName()));
            }

            if(pageReqVO.getPrice() != null) {
                predicates.add(cb.equal(root.get("price"), pageReqVO.getPrice()));
            }

            if(pageReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), pageReqVO.getProjectId()));
            }

            if(pageReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), pageReqVO.getStatus()));
            }

            if(pageReqVO.getPaidTime() != null) {
                predicates.add(cb.between(root.get("paidTime"), pageReqVO.getPaidTime()[0], pageReqVO.getPaidTime()[1]));
            } 
            if(pageReqVO.getDeadline() != null) {
                predicates.add(cb.between(root.get("deadline"), pageReqVO.getDeadline()[0], pageReqVO.getDeadline()[1]));
            } 
            if(pageReqVO.getReceiptUrl() != null) {
                predicates.add(cb.equal(root.get("receiptUrl"), pageReqVO.getReceiptUrl()));
            }

            if(pageReqVO.getReceiptName() != null) {
                predicates.add(cb.like(root.get("receiptName"), "%" + pageReqVO.getReceiptName() + "%"));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ProjectFund> page = projectFundRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<ProjectFund> getProjectFundList(ProjectFundExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<ProjectFund> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getName() != null) {
                predicates.add(cb.equal(root.get("name"), exportReqVO.getName()));
            }

            if(exportReqVO.getPrice() != null) {
                predicates.add(cb.equal(root.get("price"), exportReqVO.getPrice()));
            }

            if(exportReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), exportReqVO.getProjectId()));
            }

            if(exportReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), exportReqVO.getStatus()));
            }

            if(exportReqVO.getPaidTime() != null) {
                predicates.add(cb.between(root.get("paidTime"), exportReqVO.getPaidTime()[0], exportReqVO.getPaidTime()[1]));
            } 
            if(exportReqVO.getDeadline() != null) {
                predicates.add(cb.between(root.get("deadline"), exportReqVO.getDeadline()[0], exportReqVO.getDeadline()[1]));
            } 
            if(exportReqVO.getReceiptUrl() != null) {
                predicates.add(cb.equal(root.get("receiptUrl"), exportReqVO.getReceiptUrl()));
            }

            if(exportReqVO.getReceiptName() != null) {
                predicates.add(cb.like(root.get("receiptName"), "%" + exportReqVO.getReceiptName() + "%"));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return projectFundRepository.findAll(spec);
    }

    private Sort createSort(ProjectFundPageOrder order) {
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

        if (order.getPrice() != null) {
            orders.add(new Sort.Order(order.getPrice().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "price"));
        }

        if (order.getProjectId() != null) {
            orders.add(new Sort.Order(order.getProjectId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "projectId"));
        }

        if (order.getStatus() != null) {
            orders.add(new Sort.Order(order.getStatus().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "status"));
        }

        if (order.getPaidTime() != null) {
            orders.add(new Sort.Order(order.getPaidTime().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "paidTime"));
        }

        if (order.getDeadline() != null) {
            orders.add(new Sort.Order(order.getDeadline().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "deadline"));
        }

        if (order.getReceiptUrl() != null) {
            orders.add(new Sort.Order(order.getReceiptUrl().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "receiptUrl"));
        }

        if (order.getReceiptName() != null) {
            orders.add(new Sort.Order(order.getReceiptName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "receiptName"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}