package cn.iocoder.yudao.module.jl.service.crm;

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
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCustomerPlan;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadCustomerPlanMapper;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadCustomerPlanRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索中的客户方案 Service 实现类
 *
 */
@Service
@Validated
public class SalesleadCustomerPlanServiceImpl implements SalesleadCustomerPlanService {

    @Resource
    private SalesleadCustomerPlanRepository salesleadCustomerPlanRepository;

    @Resource
    private SalesleadCustomerPlanMapper salesleadCustomerPlanMapper;

    @Override
    public Long createSalesleadCustomerPlan(SalesleadCustomerPlanCreateReqVO createReqVO) {
        // 插入
        SalesleadCustomerPlan salesleadCustomerPlan = salesleadCustomerPlanMapper.toEntity(createReqVO);
        salesleadCustomerPlanRepository.save(salesleadCustomerPlan);
        // 返回
        return salesleadCustomerPlan.getId();
    }

    @Override
    public void updateSalesleadCustomerPlan(SalesleadCustomerPlanUpdateReqVO updateReqVO) {
        // 校验存在
        validateSalesleadCustomerPlanExists(updateReqVO.getId());
        // 更新
        SalesleadCustomerPlan updateObj = salesleadCustomerPlanMapper.toEntity(updateReqVO);
        salesleadCustomerPlanRepository.save(updateObj);
    }

    @Override
    public void deleteSalesleadCustomerPlan(Long id) {
        // 校验存在
        validateSalesleadCustomerPlanExists(id);
        // 删除
        salesleadCustomerPlanRepository.deleteById(id);
    }

    private void validateSalesleadCustomerPlanExists(Long id) {
        salesleadCustomerPlanRepository.findById(id).orElseThrow(() -> exception(SALESLEAD_CUSTOMER_PLAN_NOT_EXISTS));
    }

    @Override
    public Optional<SalesleadCustomerPlan> getSalesleadCustomerPlan(Long id) {
        return salesleadCustomerPlanRepository.findById(id);
    }

    @Override
    public List<SalesleadCustomerPlan> getSalesleadCustomerPlanList(Collection<Long> ids) {
        return StreamSupport.stream(salesleadCustomerPlanRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<SalesleadCustomerPlan> getSalesleadCustomerPlanPage(SalesleadCustomerPlanPageReqVO pageReqVO, SalesleadCustomerPlanPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<SalesleadCustomerPlan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), pageReqVO.getSalesleadId()));
            }

            if(pageReqVO.getFileUrl() != null) {
                predicates.add(cb.equal(root.get("fileUrl"), pageReqVO.getFileUrl()));
            }

            if(pageReqVO.getFileName() != null) {
                predicates.add(cb.like(root.get("fileName"), "%" + pageReqVO.getFileName() + "%"));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<SalesleadCustomerPlan> page = salesleadCustomerPlanRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<SalesleadCustomerPlan> getSalesleadCustomerPlanList(SalesleadCustomerPlanExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<SalesleadCustomerPlan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), exportReqVO.getSalesleadId()));
            }

            if(exportReqVO.getFileUrl() != null) {
                predicates.add(cb.equal(root.get("fileUrl"), exportReqVO.getFileUrl()));
            }

            if(exportReqVO.getFileName() != null) {
                predicates.add(cb.like(root.get("fileName"), "%" + exportReqVO.getFileName() + "%"));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return salesleadCustomerPlanRepository.findAll(spec);
    }

    private Sort createSort(SalesleadCustomerPlanPageOrder order) {
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

        if (order.getFileUrl() != null) {
            orders.add(new Sort.Order(order.getFileUrl().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "fileUrl"));
        }

        if (order.getFileName() != null) {
            orders.add(new Sort.Order(order.getFileName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "fileName"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}