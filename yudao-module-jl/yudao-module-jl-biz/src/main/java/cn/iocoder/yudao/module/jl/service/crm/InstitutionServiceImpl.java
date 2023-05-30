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
import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.InstitutionMapper;
import cn.iocoder.yudao.module.jl.repository.crm.InstitutionRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 机构/公司 Service 实现类
 *
 */
@Service
@Validated
public class InstitutionServiceImpl implements InstitutionService {

    @Resource
    private InstitutionRepository institutionRepository;

    @Resource
    private InstitutionMapper institutionMapper;

    @Override
    public Long createInstitution(InstitutionCreateReqVO createReqVO) {
        // 插入
        Institution institution = institutionMapper.toEntity(createReqVO);
        institutionRepository.save(institution);
        // 返回
        return institution.getId();
    }

    @Override
    public void updateInstitution(InstitutionUpdateReqVO updateReqVO) {
        // 校验存在
        validateInstitutionExists(updateReqVO.getId());
        // 更新
        Institution updateObj = institutionMapper.toEntity(updateReqVO);
        institutionRepository.save(updateObj);
    }

    @Override
    public void deleteInstitution(Long id) {
        // 校验存在
        validateInstitutionExists(id);
        // 删除
        institutionRepository.deleteById(id);
    }

    private void validateInstitutionExists(Long id) {
        institutionRepository.findById(id).orElseThrow(() -> exception(INSTITUTION_NOT_EXISTS));
    }

    @Override
    public Optional<Institution> getInstitution(Long id) {
        return institutionRepository.findById(id);
    }

    @Override
    public List<Institution> getInstitutionList(Collection<Long> ids) {
        return StreamSupport.stream(institutionRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Institution> getInstitutionPage(InstitutionPageReqVO pageReqVO, InstitutionPageOrder orderVo) {
        // 创建 Sort 对象
        Sort sort = createSort(orderVo);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<Institution> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (pageReqVO.getProvince() != null) {
                predicates.add(cb.equal(root.get("province"), pageReqVO.getProvince()));
            }

            if (pageReqVO.getCity() != null) {
                predicates.add(cb.equal(root.get("city"), pageReqVO.getCity()));
            }

            if (pageReqVO.getName() != null) {
                predicates.add(cb.equal(root.get("name"), pageReqVO.getName()));
            }

            if (pageReqVO.getAddress() != null) {
                predicates.add(cb.equal(root.get("address"), pageReqVO.getAddress()));
            }

            if (pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }

            if (pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<Institution> page = institutionRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<Institution> getInstitutionList(InstitutionExportReqVO exportReqVO) {
        return null;
    }

    private Sort createSort(InstitutionPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getProvince() != null) {
            orders.add(new Sort.Order(order.getProvince().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "province"));
        }

        if (order.getCity() != null) {
            orders.add(new Sort.Order(order.getCity().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "city"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getAddress() != null) {
            orders.add(new Sort.Order(order.getAddress().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "address"));
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