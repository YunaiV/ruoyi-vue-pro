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
import cn.iocoder.yudao.module.jl.entity.crm.Followup;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.FollowupMapper;
import cn.iocoder.yudao.module.jl.repository.crm.FollowupRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售跟进 Service 实现类
 *
 */
@Service
@Validated
public class FollowupServiceImpl implements FollowupService {

    @Resource
    private FollowupRepository followupRepository;

    @Resource
    private FollowupMapper followupMapper;

    @Override
    public Long createFollowup(FollowupCreateReqVO createReqVO) {
        // 插入
        Followup followup = followupMapper.toEntity(createReqVO);
        followupRepository.save(followup);
        // 返回
        return followup.getId();
    }

    @Override
    public void updateFollowup(FollowupUpdateReqVO updateReqVO) {
        // 校验存在
        validateFollowupExists(updateReqVO.getId());
        // 更新
        Followup updateObj = followupMapper.toEntity(updateReqVO);
        followupRepository.save(updateObj);
    }

    @Override
    public void deleteFollowup(Long id) {
        // 校验存在
        validateFollowupExists(id);
        // 删除
        followupRepository.deleteById(id);
    }

    private void validateFollowupExists(Long id) {
        followupRepository.findById(id).orElseThrow(() -> exception(FOLLOWUP_NOT_EXISTS));
    }

    @Override
    public Optional<Followup> getFollowup(Long id) {
        return followupRepository.findById(id);
    }

    @Override
    public List<Followup> getFollowupList(Collection<Long> ids) {
        return StreamSupport.stream(followupRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Followup> getFollowupPage(FollowupPageReqVO pageReqVO, FollowupPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<Followup> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getContent() != null) {
                predicates.add(cb.equal(root.get("content"), pageReqVO.getContent()));
            }

            if(pageReqVO.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), pageReqVO.getCustomerId()));
            }

            if(pageReqVO.getRefId() != null) {
                predicates.add(cb.equal(root.get("refId"), pageReqVO.getRefId()));
            }

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<Followup> page = followupRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<Followup> getFollowupList(FollowupExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<Followup> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getContent() != null) {
                predicates.add(cb.equal(root.get("content"), exportReqVO.getContent()));
            }

            if(exportReqVO.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), exportReqVO.getCustomerId()));
            }

            if(exportReqVO.getRefId() != null) {
                predicates.add(cb.equal(root.get("refId"), exportReqVO.getRefId()));
            }

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return followupRepository.findAll(spec);
    }

    private Sort createSort(FollowupPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getContent() != null) {
            orders.add(new Sort.Order(order.getContent().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "content"));
        }

        if (order.getCustomerId() != null) {
            orders.add(new Sort.Order(order.getCustomerId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "customerId"));
        }

        if (order.getRefId() != null) {
            orders.add(new Sort.Order(order.getRefId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "refId"));
        }

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}