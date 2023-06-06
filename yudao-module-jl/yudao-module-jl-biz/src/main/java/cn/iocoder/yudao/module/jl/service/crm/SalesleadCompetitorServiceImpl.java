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
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadCompetitorMapper;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadCompetitorRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索中竞争对手的报价 Service 实现类
 *
 */
@Service
@Validated
public class SalesleadCompetitorServiceImpl implements SalesleadCompetitorService {

    @Resource
    private SalesleadCompetitorRepository salesleadCompetitorRepository;

    @Resource
    private SalesleadCompetitorMapper salesleadCompetitorMapper;

    @Override
    public Long createSalesleadCompetitor(SalesleadCompetitorCreateReqVO createReqVO) {
        // 插入
        SalesleadCompetitor salesleadCompetitor = salesleadCompetitorMapper.toEntity(createReqVO);
        salesleadCompetitorRepository.save(salesleadCompetitor);
        // 返回
        return salesleadCompetitor.getId();
    }

    @Override
    public void updateSalesleadCompetitor(SalesleadCompetitorUpdateReqVO updateReqVO) {
        // 校验存在
        validateSalesleadCompetitorExists(updateReqVO.getId());
        // 更新
        SalesleadCompetitor updateObj = salesleadCompetitorMapper.toEntity(updateReqVO);
        salesleadCompetitorRepository.save(updateObj);
    }

    @Override
    public void deleteSalesleadCompetitor(Long id) {
        // 校验存在
        validateSalesleadCompetitorExists(id);
        // 删除
        salesleadCompetitorRepository.deleteById(id);
    }

    private void validateSalesleadCompetitorExists(Long id) {
        salesleadCompetitorRepository.findById(id).orElseThrow(() -> exception(SALESLEAD_COMPETITOR_NOT_EXISTS));
    }

    @Override
    public Optional<SalesleadCompetitor> getSalesleadCompetitor(Long id) {
        return salesleadCompetitorRepository.findById(id);
    }

    @Override
    public List<SalesleadCompetitor> getSalesleadCompetitorList(Collection<Long> ids) {
        return StreamSupport.stream(salesleadCompetitorRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<SalesleadCompetitor> getSalesleadCompetitorPage(SalesleadCompetitorPageReqVO pageReqVO, SalesleadCompetitorPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<SalesleadCompetitor> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), pageReqVO.getSalesleadId()));
            }

            if(pageReqVO.getCompetitorId() != null) {
                predicates.add(cb.equal(root.get("competitorId"), pageReqVO.getCompetitorId()));
            }

            if(pageReqVO.getCompetitorQuotation() != null) {
                predicates.add(cb.equal(root.get("competitorQuotation"), pageReqVO.getCompetitorQuotation()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<SalesleadCompetitor> page = salesleadCompetitorRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<SalesleadCompetitor> getSalesleadCompetitorList(SalesleadCompetitorExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<SalesleadCompetitor> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), exportReqVO.getSalesleadId()));
            }

            if(exportReqVO.getCompetitorId() != null) {
                predicates.add(cb.equal(root.get("competitorId"), exportReqVO.getCompetitorId()));
            }

            if(exportReqVO.getCompetitorQuotation() != null) {
                predicates.add(cb.equal(root.get("competitorQuotation"), exportReqVO.getCompetitorQuotation()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return salesleadCompetitorRepository.findAll(spec);
    }

    private Sort createSort(SalesleadCompetitorPageOrder order) {
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

        if (order.getCompetitorId() != null) {
            orders.add(new Sort.Order(order.getCompetitorId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "competitorId"));
        }

        if (order.getCompetitorQuotation() != null) {
            orders.add(new Sort.Order(order.getCompetitorQuotation().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "competitorQuotation"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}