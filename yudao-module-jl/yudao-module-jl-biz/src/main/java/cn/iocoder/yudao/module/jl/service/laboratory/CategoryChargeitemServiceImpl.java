package cn.iocoder.yudao.module.jl.service.laboratory;

import cn.iocoder.yudao.module.jl.controller.admin.laboratory.CategoryChargeItemSaveReqVO;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySupply;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategorySupplyRepository;
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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryChargeitem;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.CategoryChargeitemMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategoryChargeitemRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验名目的收费项 Service 实现类
 *
 */
@Service
@Validated
public class CategoryChargeitemServiceImpl implements CategoryChargeitemService {

    @Resource
    private CategoryChargeitemRepository categoryChargeitemRepository;

    @Resource
    private CategoryChargeitemMapper categoryChargeitemMapper;
    private final CategorySupplyRepository categorySupplyRepository;

    public CategoryChargeitemServiceImpl(CategorySupplyRepository categorySupplyRepository) {
        this.categorySupplyRepository = categorySupplyRepository;
    }

    @Override
    public Long createCategoryChargeitem(CategoryChargeitemCreateReqVO createReqVO) {
        // 插入
        CategoryChargeitem categoryChargeitem = categoryChargeitemMapper.toEntity(createReqVO);
        categoryChargeitemRepository.save(categoryChargeitem);
        // 返回
        return categoryChargeitem.getId();
    }

    @Override
    public void updateCategoryChargeitem(CategoryChargeitemUpdateReqVO updateReqVO) {
        // 校验存在
        validateCategoryChargeitemExists(updateReqVO.getId());
        // 更新
        CategoryChargeitem updateObj = categoryChargeitemMapper.toEntity(updateReqVO);
        categoryChargeitemRepository.save(updateObj);
    }

    @Override
    public void deleteCategoryChargeitem(Long id) {
        // 校验存在
        validateCategoryChargeitemExists(id);
        // 删除
        categoryChargeitemRepository.deleteById(id);
    }

    private void validateCategoryChargeitemExists(Long id) {
        categoryChargeitemRepository.findById(id).orElseThrow(() -> exception(CATEGORY_CHARGEITEM_NOT_EXISTS));
    }

    @Override
    public Optional<CategoryChargeitem> getCategoryChargeitem(Long id) {
        return categoryChargeitemRepository.findById(id);
    }

    @Override
    public List<CategoryChargeitem> getCategoryChargeitemList(Collection<Long> ids) {
        return StreamSupport.stream(categoryChargeitemRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<CategoryChargeitem> getCategoryChargeitemPage(CategoryChargeitemPageReqVO pageReqVO, CategoryChargeitemPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<CategoryChargeitem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            if(pageReqVO.getQuantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), pageReqVO.getQuantity()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }

            if(pageReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), pageReqVO.getCategoryId()));
            }

            if(pageReqVO.getUnitAmount() != null) {
                predicates.add(cb.equal(root.get("unitAmount"), pageReqVO.getUnitAmount()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<CategoryChargeitem> page = categoryChargeitemRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<CategoryChargeitem> getCategoryChargeitemList(CategoryChargeitemExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<CategoryChargeitem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            if(exportReqVO.getQuantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), exportReqVO.getQuantity()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }

            if(exportReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), exportReqVO.getCategoryId()));
            }

            if(exportReqVO.getUnitAmount() != null) {
                predicates.add(cb.equal(root.get("unitAmount"), exportReqVO.getUnitAmount()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return categoryChargeitemRepository.findAll(spec);
    }

    /**
     * @param saveReqVO
     * @return
     */
    @Override
    public Boolean saveCategoryChargeItem(CategoryChargeItemSaveReqVO saveReqVO) {
        List<CategoryChargeitem> categoryChargeitemList = new ArrayList<>();

        if(saveReqVO.getCategoryId() != null && saveReqVO.getCategoryId() > 0) {
            // 删除原有的
            categoryChargeitemRepository.deleteByCategoryId(saveReqVO.getCategoryId());
        }

        // 保存新的
        for (CategoryChargeitemBaseWithoutIDVO item : saveReqVO.getCategoryChargeitemList()) {
            CategoryChargeitem categoryChargeitem = new CategoryChargeitem();
            categoryChargeitem.setCategoryId(saveReqVO.getCategoryId());
            categoryChargeitem.setChargeItemId(item.getChargeItemId());
            categoryChargeitem.setName(item.getName());
            categoryChargeitem.setFeeStandard(item.getFeeStandard());
            categoryChargeitem.setUnitFee(item.getUnitFee());
            categoryChargeitem.setQuantity(item.getQuantity());
            categoryChargeitem.setMark(item.getMark());
            categoryChargeitem.setUnitAmount(item.getUnitAmount());

            categoryChargeitemList.add(categoryChargeitem);
        }

        categoryChargeitemRepository.saveAll(categoryChargeitemList);
        return true;
    }

    private Sort createSort(CategoryChargeitemPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
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

        if (order.getQuantity() != null) {
            orders.add(new Sort.Order(order.getQuantity().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "quantity"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }

        if (order.getCategoryId() != null) {
            orders.add(new Sort.Order(order.getCategoryId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "categoryId"));
        }

        if (order.getUnitAmount() != null) {
            orders.add(new Sort.Order(order.getUnitAmount().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "unitAmount"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}