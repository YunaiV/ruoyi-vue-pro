package cn.iocoder.yudao.module.jl.service.laboratory;

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
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySop;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.CategorySopMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategorySopRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验名目的操作SOP Service 实现类
 *
 */
@Service
@Validated
public class CategorySopServiceImpl implements CategorySopService {

    @Resource
    private CategorySopRepository categorySopRepository;

    @Resource
    private CategorySopMapper categorySopMapper;

    @Override
    public Long createCategorySop(CategorySopCreateReqVO createReqVO) {
        // 插入
        CategorySop categorySop = categorySopMapper.toEntity(createReqVO);
        categorySopRepository.save(categorySop);
        // 返回
        return categorySop.getId();
    }

    @Override
    public void updateCategorySop(CategorySopUpdateReqVO updateReqVO) {
        // 校验存在
        validateCategorySopExists(updateReqVO.getId());
        // 更新
        CategorySop updateObj = categorySopMapper.toEntity(updateReqVO);
        categorySopRepository.save(updateObj);
    }

    @Override
    public void deleteCategorySop(Long id) {
        // 校验存在
        validateCategorySopExists(id);
        // 删除
        categorySopRepository.deleteById(id);
    }

    private void validateCategorySopExists(Long id) {
        categorySopRepository.findById(id).orElseThrow(() -> exception(CATEGORY_SOP_NOT_EXISTS));
    }

    @Override
    public Optional<CategorySop> getCategorySop(Long id) {
        return categorySopRepository.findById(id);
    }

    @Override
    public List<CategorySop> getCategorySopList(Collection<Long> ids) {
        return StreamSupport.stream(categorySopRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<CategorySop> getCategorySopPage(CategorySopPageReqVO pageReqVO, CategorySopPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<CategorySop> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), pageReqVO.getCategoryId()));
            }

            if(pageReqVO.getContent() != null) {
                predicates.add(cb.equal(root.get("content"), pageReqVO.getContent()));
            }

            if(pageReqVO.getStep() != null) {
                predicates.add(cb.equal(root.get("step"), pageReqVO.getStep()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }

            if(pageReqVO.getDependIds() != null) {
                predicates.add(cb.equal(root.get("dependIds"), pageReqVO.getDependIds()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<CategorySop> page = categorySopRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<CategorySop> getCategorySopList(CategorySopExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<CategorySop> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), exportReqVO.getCategoryId()));
            }

            if(exportReqVO.getContent() != null) {
                predicates.add(cb.equal(root.get("content"), exportReqVO.getContent()));
            }

            if(exportReqVO.getStep() != null) {
                predicates.add(cb.equal(root.get("step"), exportReqVO.getStep()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }

            if(exportReqVO.getDependIds() != null) {
                predicates.add(cb.equal(root.get("dependIds"), exportReqVO.getDependIds()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return categorySopRepository.findAll(spec);
    }

    private Sort createSort(CategorySopPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getCategoryId() != null) {
            orders.add(new Sort.Order(order.getCategoryId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "categoryId"));
        }

        if (order.getContent() != null) {
            orders.add(new Sort.Order(order.getContent().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "content"));
        }

        if (order.getStep() != null) {
            orders.add(new Sort.Order(order.getStep().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "step"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }

        if (order.getDependIds() != null) {
            orders.add(new Sort.Order(order.getDependIds().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "dependIds"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}