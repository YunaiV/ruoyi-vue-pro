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
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadManager;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadManagerMapper;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadManagerRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索中的项目售前支持人员 Service 实现类
 *
 */
@Service
@Validated
public class SalesleadManagerServiceImpl implements SalesleadManagerService {

    @Resource
    private SalesleadManagerRepository salesleadManagerRepository;

    @Resource
    private SalesleadManagerMapper salesleadManagerMapper;

    @Override
    public Long createSalesleadManager(SalesleadManagerCreateReqVO createReqVO) {
        // 插入
        SalesleadManager salesleadManager = salesleadManagerMapper.toEntity(createReqVO);
        salesleadManagerRepository.save(salesleadManager);
        // 返回
        return salesleadManager.getId();
    }

    @Override
    public void updateSalesleadManager(SalesleadManagerUpdateReqVO updateReqVO) {
        // 校验存在
        validateSalesleadManagerExists(updateReqVO.getId());
        // 更新
        SalesleadManager updateObj = salesleadManagerMapper.toEntity(updateReqVO);
        salesleadManagerRepository.save(updateObj);
    }

    @Override
    public void deleteSalesleadManager(Long id) {
        // 校验存在
        validateSalesleadManagerExists(id);
        // 删除
        salesleadManagerRepository.deleteById(id);
    }

    private void validateSalesleadManagerExists(Long id) {
        salesleadManagerRepository.findById(id).orElseThrow(() -> exception(SALESLEAD_MANAGER_NOT_EXISTS));
    }

    @Override
    public Optional<SalesleadManager> getSalesleadManager(Long id) {
        return salesleadManagerRepository.findById(id);
    }

    @Override
    public List<SalesleadManager> getSalesleadManagerList(Collection<Long> ids) {
        return StreamSupport.stream(salesleadManagerRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<SalesleadManager> getSalesleadManagerPage(SalesleadManagerPageReqVO pageReqVO, SalesleadManagerPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<SalesleadManager> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), pageReqVO.getSalesleadId()));
            }

            if(pageReqVO.getManagerId() != null) {
                predicates.add(cb.equal(root.get("managerId"), pageReqVO.getManagerId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<SalesleadManager> page = salesleadManagerRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<SalesleadManager> getSalesleadManagerList(SalesleadManagerExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<SalesleadManager> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), exportReqVO.getSalesleadId()));
            }

            if(exportReqVO.getManagerId() != null) {
                predicates.add(cb.equal(root.get("managerId"), exportReqVO.getManagerId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return salesleadManagerRepository.findAll(spec);
    }

    private Sort createSort(SalesleadManagerPageOrder order) {
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

        if (order.getManagerId() != null) {
            orders.add(new Sort.Order(order.getManagerId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "managerId"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}