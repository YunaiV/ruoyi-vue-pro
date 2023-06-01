package cn.iocoder.yudao.module.jl.service.laboratory;

import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySop;
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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySupply;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.CategorySupplyMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategorySupplyRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验名目的物资 Service 实现类
 *
 */
@Service
@Validated
public class CategorySupplyServiceImpl implements CategorySupplyService {

    @Resource
    private CategorySupplyRepository categorySupplyRepository;

    @Resource
    private CategorySupplyMapper categorySupplyMapper;

    @Override
    public Long createCategorySupply(CategorySupplyCreateReqVO createReqVO) {
        // 插入
        CategorySupply categorySupply = categorySupplyMapper.toEntity(createReqVO);
        categorySupplyRepository.save(categorySupply);
        // 返回
        return categorySupply.getId();
    }

    @Override
    public void updateCategorySupply(CategorySupplyUpdateReqVO updateReqVO) {
        // 校验存在
        validateCategorySupplyExists(updateReqVO.getId());
        // 更新
        CategorySupply updateObj = categorySupplyMapper.toEntity(updateReqVO);
        categorySupplyRepository.save(updateObj);
    }

    @Override
    public void deleteCategorySupply(Long id) {
        // 校验存在
        validateCategorySupplyExists(id);
        // 删除
        categorySupplyRepository.deleteById(id);
    }

    private void validateCategorySupplyExists(Long id) {
        categorySupplyRepository.findById(id).orElseThrow(() -> exception(CATEGORY_SUPPLY_NOT_EXISTS));
    }

    @Override
    public Optional<CategorySupply> getCategorySupply(Long id) {
        return categorySupplyRepository.findById(id);
    }

    @Override
    public List<CategorySupply> getCategorySupplyList(Collection<Long> ids) {
        return StreamSupport.stream(categorySupplyRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<CategorySupply> getCategorySupplyPage(CategorySupplyPageReqVO pageReqVO, CategorySupplyPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<CategorySupply> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getSupplyId() != null) {
                predicates.add(cb.equal(root.get("supplyId"), pageReqVO.getSupplyId()));
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

            if(pageReqVO.getQuantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), pageReqVO.getQuantity()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }

            if(pageReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), pageReqVO.getCategoryId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<CategorySupply> page = categorySupplyRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<CategorySupply> getCategorySupplyList(CategorySupplyExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<CategorySupply> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getSupplyId() != null) {
                predicates.add(cb.equal(root.get("supplyId"), exportReqVO.getSupplyId()));
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

            if(exportReqVO.getQuantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), exportReqVO.getQuantity()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }

            if(exportReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), exportReqVO.getCategoryId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return categorySupplyRepository.findAll(spec);
    }

    /**
     * @param saveReqVO
     * @return
     */
    @Override
    public boolean saveCategorySupply(CategorySupplySaveReqVO saveReqVO) {
        List<CategorySupply> categorySupplyList = new ArrayList<>();

        if(saveReqVO.getCategoryId() != null && saveReqVO.getCategoryId() > 0) {
            // 删除原有的
            categorySupplyRepository.deleteByCategoryId(saveReqVO.getCategoryId());
        }

        // 保存新的
        for (CategorySupplyBaseWithoutIDVO supply : saveReqVO.getCategorySupplyList()) {
            CategorySupply categorySupply = new CategorySupply();
            categorySupply.setCategoryId(saveReqVO.getCategoryId());
            categorySupply.setName(supply.getName());
            categorySupply.setFeeStandard(supply.getFeeStandard());
            categorySupply.setUnitFee(supply.getUnitFee());
            categorySupply.setQuantity(supply.getQuantity());
            categorySupply.setMark(supply.getMark());

            categorySupplyList.add(categorySupply);
        }

        categorySupplyRepository.saveAll(categorySupplyList);
        return true;
    }

    private Sort createSort(CategorySupplyPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getSupplyId() != null) {
            orders.add(new Sort.Order(order.getSupplyId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "supplyId"));
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

        if (order.getQuantity() != null) {
            orders.add(new Sort.Order(order.getQuantity().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "quantity"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }

        if (order.getCategoryId() != null) {
            orders.add(new Sort.Order(order.getCategoryId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "categoryId"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}