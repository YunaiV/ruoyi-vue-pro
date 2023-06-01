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
import cn.iocoder.yudao.module.jl.entity.laboratory.LabSupply;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.LabSupplyMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.LabSupplyRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验物资 Service 实现类
 *
 */
@Service
@Validated
public class LabSupplyServiceImpl implements LabSupplyService {

    @Resource
    private LabSupplyRepository labSupplyRepository;

    @Resource
    private LabSupplyMapper labSupplyMapper;

    @Override
    public Long createLabSupply(LabSupplyCreateReqVO createReqVO) {
        // 插入
        LabSupply labSupply = labSupplyMapper.toEntity(createReqVO);
        labSupplyRepository.save(labSupply);
        // 返回
        return labSupply.getId();
    }

    @Override
    public void updateLabSupply(LabSupplyUpdateReqVO updateReqVO) {
        // 校验存在
        validateLabSupplyExists(updateReqVO.getId());
        // 更新
        LabSupply updateObj = labSupplyMapper.toEntity(updateReqVO);
        labSupplyRepository.save(updateObj);
    }

    @Override
    public void deleteLabSupply(Long id) {
        // 校验存在
        validateLabSupplyExists(id);
        // 删除
        labSupplyRepository.deleteById(id);
    }

    private void validateLabSupplyExists(Long id) {
        labSupplyRepository.findById(id).orElseThrow(() -> exception(LAB_SUPPLY_NOT_EXISTS));
    }

    @Override
    public Optional<LabSupply> getLabSupply(Long id) {
        return labSupplyRepository.findById(id);
    }

    @Override
    public List<LabSupply> getLabSupplyList(Collection<Long> ids) {
        return StreamSupport.stream(labSupplyRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<LabSupply> getLabSupplyPage(LabSupplyPageReqVO pageReqVO, LabSupplyPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<LabSupply> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<LabSupply> page = labSupplyRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<LabSupply> getLabSupplyList(LabSupplyExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<LabSupply> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return labSupplyRepository.findAll(spec);
    }

    private Sort createSort(LabSupplyPageOrder order) {
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

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}