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
import cn.iocoder.yudao.module.jl.entity.project.ProjectChargeitem;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.project.ProjectChargeitemMapper;
import cn.iocoder.yudao.module.jl.repository.project.ProjectChargeitemRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 项目中的实验名目的收费项 Service 实现类
 *
 */
@Service
@Validated
public class ProjectChargeitemServiceImpl implements ProjectChargeitemService {

    @Resource
    private ProjectChargeitemRepository projectChargeitemRepository;

    @Resource
    private ProjectChargeitemMapper projectChargeitemMapper;

    @Override
    public Long createProjectChargeitem(ProjectChargeitemCreateReqVO createReqVO) {
        // 插入
        ProjectChargeitem projectChargeitem = projectChargeitemMapper.toEntity(createReqVO);
        projectChargeitemRepository.save(projectChargeitem);
        // 返回
        return projectChargeitem.getId();
    }

    @Override
    public void updateProjectChargeitem(ProjectChargeitemUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectChargeitemExists(updateReqVO.getId());
        // 更新
        ProjectChargeitem updateObj = projectChargeitemMapper.toEntity(updateReqVO);
        projectChargeitemRepository.save(updateObj);
    }

    @Override
    public void deleteProjectChargeitem(Long id) {
        // 校验存在
        validateProjectChargeitemExists(id);
        // 删除
        projectChargeitemRepository.deleteById(id);
    }

    private void validateProjectChargeitemExists(Long id) {
        projectChargeitemRepository.findById(id).orElseThrow(() -> exception(PROJECT_CHARGEITEM_NOT_EXISTS));
    }

    @Override
    public Optional<ProjectChargeitem> getProjectChargeitem(Long id) {
        return projectChargeitemRepository.findById(id);
    }

    @Override
    public List<ProjectChargeitem> getProjectChargeitemList(Collection<Long> ids) {
        return StreamSupport.stream(projectChargeitemRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProjectChargeitem> getProjectChargeitemPage(ProjectChargeitemPageReqVO pageReqVO, ProjectChargeitemPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<ProjectChargeitem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getProjectCategoryId() != null) {
                predicates.add(cb.equal(root.get("projectCategoryId"), pageReqVO.getProjectCategoryId()));
            }

            if(pageReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), pageReqVO.getCategoryId()));
            }

            if(pageReqVO.getChargeItemId() != null) {
                predicates.add(cb.equal(root.get("chargeItemId"), pageReqVO.getChargeItemId()));
            }

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getFeeStandard() != null) {
                predicates.add(cb.equal(root.get("feeStandard"), pageReqVO.getFeeStandard()));
            }

            if(pageReqVO.getUnitFee() != null) {
                predicates.add(cb.equal(root.get("unitFee"), pageReqVO.getUnitFee()));
            }

            if(pageReqVO.getUnitAmount() != null) {
                predicates.add(cb.equal(root.get("unitAmount"), pageReqVO.getUnitAmount()));
            }

            if(pageReqVO.getQuantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), pageReqVO.getQuantity()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ProjectChargeitem> page = projectChargeitemRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<ProjectChargeitem> getProjectChargeitemList(ProjectChargeitemExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<ProjectChargeitem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getProjectCategoryId() != null) {
                predicates.add(cb.equal(root.get("projectCategoryId"), exportReqVO.getProjectCategoryId()));
            }

            if(exportReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), exportReqVO.getCategoryId()));
            }

            if(exportReqVO.getChargeItemId() != null) {
                predicates.add(cb.equal(root.get("chargeItemId"), exportReqVO.getChargeItemId()));
            }

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getFeeStandard() != null) {
                predicates.add(cb.equal(root.get("feeStandard"), exportReqVO.getFeeStandard()));
            }

            if(exportReqVO.getUnitFee() != null) {
                predicates.add(cb.equal(root.get("unitFee"), exportReqVO.getUnitFee()));
            }

            if(exportReqVO.getUnitAmount() != null) {
                predicates.add(cb.equal(root.get("unitAmount"), exportReqVO.getUnitAmount()));
            }

            if(exportReqVO.getQuantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), exportReqVO.getQuantity()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return projectChargeitemRepository.findAll(spec);
    }

    private Sort createSort(ProjectChargeitemPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getProjectCategoryId() != null) {
            orders.add(new Sort.Order(order.getProjectCategoryId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "projectCategoryId"));
        }

        if (order.getCategoryId() != null) {
            orders.add(new Sort.Order(order.getCategoryId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "categoryId"));
        }

        if (order.getChargeItemId() != null) {
            orders.add(new Sort.Order(order.getChargeItemId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "chargeItemId"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getFeeStandard() != null) {
            orders.add(new Sort.Order(order.getFeeStandard().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "feeStandard"));
        }

        if (order.getUnitFee() != null) {
            orders.add(new Sort.Order(order.getUnitFee().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "unitFee"));
        }

        if (order.getUnitAmount() != null) {
            orders.add(new Sort.Order(order.getUnitAmount().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "unitAmount"));
        }

        if (order.getQuantity() != null) {
            orders.add(new Sort.Order(order.getQuantity().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "quantity"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}