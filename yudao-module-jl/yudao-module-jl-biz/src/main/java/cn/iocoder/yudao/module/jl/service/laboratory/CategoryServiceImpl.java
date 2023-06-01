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
import cn.iocoder.yudao.module.jl.entity.laboratory.Category;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.CategoryMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategoryRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验名目 Service 实现类
 *
 */
@Service
@Validated
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public Long createCategory(CategoryCreateReqVO createReqVO) {
        // 插入
        Category category = categoryMapper.toEntity(createReqVO);
        categoryRepository.save(category);
        // 返回
        return category.getId();
    }

    @Override
    public void updateCategory(CategoryUpdateReqVO updateReqVO) {
        // 校验存在
        validateCategoryExists(updateReqVO.getId());
        // 更新
        Category updateObj = categoryMapper.toEntity(updateReqVO);
        categoryRepository.save(updateObj);
    }

    @Override
    public void deleteCategory(Long id) {
        // 校验存在
        validateCategoryExists(id);
        // 删除
        categoryRepository.deleteById(id);
    }

    private void validateCategoryExists(Long id) {
        categoryRepository.findById(id).orElseThrow(() -> exception(CATEGORY_NOT_EXISTS));
    }

    @Override
    public Optional<Category> getCategory(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> getCategoryList(Collection<Long> ids) {
        return StreamSupport.stream(categoryRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Category> getCategoryPage(CategoryPageReqVO pageReqVO, CategoryPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<Category> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getDifficultyLevel() != null) {
                predicates.add(cb.equal(root.get("difficultyLevel"), pageReqVO.getDifficultyLevel()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<Category> page = categoryRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<Category> getCategoryList(CategoryExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<Category> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getDifficultyLevel() != null) {
                predicates.add(cb.equal(root.get("difficultyLevel"), exportReqVO.getDifficultyLevel()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return categoryRepository.findAll(spec);
    }

    private Sort createSort(CategoryPageOrder order) {
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

        if (order.getDifficultyLevel() != null) {
            orders.add(new Sort.Order(order.getDifficultyLevel().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "difficultyLevel"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}