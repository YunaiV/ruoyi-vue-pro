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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryReference;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.CategoryReferenceMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategoryReferenceRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验名目的参考资料 Service 实现类
 *
 */
@Service
@Validated
public class CategoryReferenceServiceImpl implements CategoryReferenceService {

    @Resource
    private CategoryReferenceRepository categoryReferenceRepository;

    @Resource
    private CategoryReferenceMapper categoryReferenceMapper;

    @Override
    public Long createCategoryReference(CategoryReferenceCreateReqVO createReqVO) {
        // 插入
        CategoryReference categoryReference = categoryReferenceMapper.toEntity(createReqVO);
        categoryReferenceRepository.save(categoryReference);
        // 返回
        return categoryReference.getId();
    }

    @Override
    public void updateCategoryReference(CategoryReferenceUpdateReqVO updateReqVO) {
        // 校验存在
        validateCategoryReferenceExists(updateReqVO.getId());
        // 更新
        CategoryReference updateObj = categoryReferenceMapper.toEntity(updateReqVO);
        categoryReferenceRepository.save(updateObj);
    }

    @Override
    public void deleteCategoryReference(Long id) {
        // 校验存在
        validateCategoryReferenceExists(id);
        // 删除
        categoryReferenceRepository.deleteById(id);
    }

    private void validateCategoryReferenceExists(Long id) {
        categoryReferenceRepository.findById(id).orElseThrow(() -> exception(CATEGORY_REFERENCE_NOT_EXISTS));
    }

    @Override
    public Optional<CategoryReference> getCategoryReference(Long id) {
        return categoryReferenceRepository.findById(id);
    }

    @Override
    public List<CategoryReference> getCategoryReferenceList(Collection<Long> ids) {
        return StreamSupport.stream(categoryReferenceRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<CategoryReference> getCategoryReferencePage(CategoryReferencePageReqVO pageReqVO, CategoryReferencePageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<CategoryReference> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), pageReqVO.getCategoryId()));
            }

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getUrl() != null) {
                predicates.add(cb.equal(root.get("url"), pageReqVO.getUrl()));
            }

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<CategoryReference> page = categoryReferenceRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<CategoryReference> getCategoryReferenceList(CategoryReferenceExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<CategoryReference> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), exportReqVO.getCategoryId()));
            }

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getUrl() != null) {
                predicates.add(cb.equal(root.get("url"), exportReqVO.getUrl()));
            }

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return categoryReferenceRepository.findAll(spec);
    }

    private Sort createSort(CategoryReferencePageOrder order) {
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

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getUrl() != null) {
            orders.add(new Sort.Order(order.getUrl().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "url"));
        }

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}