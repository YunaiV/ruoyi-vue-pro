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
import cn.iocoder.yudao.module.jl.entity.laboratory.ChargeItem;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.ChargeItemMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.ChargeItemRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验收费项 Service 实现类
 *
 */
@Service
@Validated
public class ChargeItemServiceImpl implements ChargeItemService {

    @Resource
    private ChargeItemRepository chargeItemRepository;

    @Resource
    private ChargeItemMapper chargeItemMapper;

    @Override
    public Long createChargeItem(ChargeItemCreateReqVO createReqVO) {
        // 插入
        ChargeItem chargeItem = chargeItemMapper.toEntity(createReqVO);
        chargeItemRepository.save(chargeItem);
        // 返回
        return chargeItem.getId();
    }

    @Override
    public void updateChargeItem(ChargeItemUpdateReqVO updateReqVO) {
        // 校验存在
        validateChargeItemExists(updateReqVO.getId());
        // 更新
        ChargeItem updateObj = chargeItemMapper.toEntity(updateReqVO);
        chargeItemRepository.save(updateObj);
    }

    @Override
    public void deleteChargeItem(Long id) {
        // 校验存在
        validateChargeItemExists(id);
        // 删除
        chargeItemRepository.deleteById(id);
    }

    private void validateChargeItemExists(Long id) {
        chargeItemRepository.findById(id).orElseThrow(() -> exception(CHARGE_ITEM_NOT_EXISTS));
    }

    @Override
    public Optional<ChargeItem> getChargeItem(Long id) {
        return chargeItemRepository.findById(id);
    }

    @Override
    public List<ChargeItem> getChargeItemList(Collection<Long> ids) {
        return StreamSupport.stream(chargeItemRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ChargeItem> getChargeItemPage(ChargeItemPageReqVO pageReqVO, ChargeItemPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<ChargeItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getCostPrice() != null) {
                predicates.add(cb.equal(root.get("costPrice"), pageReqVO.getCostPrice()));
            }

            if(pageReqVO.getSuggestedSellingPrice() != null) {
                predicates.add(cb.equal(root.get("suggestedSellingPrice"), pageReqVO.getSuggestedSellingPrice()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ChargeItem> page = chargeItemRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<ChargeItem> getChargeItemList(ChargeItemExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<ChargeItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getCostPrice() != null) {
                predicates.add(cb.equal(root.get("costPrice"), exportReqVO.getCostPrice()));
            }

            if(exportReqVO.getSuggestedSellingPrice() != null) {
                predicates.add(cb.equal(root.get("suggestedSellingPrice"), exportReqVO.getSuggestedSellingPrice()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return chargeItemRepository.findAll(spec);
    }

    private Sort createSort(ChargeItemPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getCostPrice() != null) {
            orders.add(new Sort.Order(order.getCostPrice().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "costPrice"));
        }

        if (order.getSuggestedSellingPrice() != null) {
            orders.add(new Sort.Order(order.getSuggestedSellingPrice().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "suggestedSellingPrice"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}