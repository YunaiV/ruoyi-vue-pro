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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySkillUser;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.laboratory.CategorySkillUserMapper;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategorySkillUserRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 实验名目的擅长人员 Service 实现类
 *
 */
@Service
@Validated
public class CategorySkillUserServiceImpl implements CategorySkillUserService {

    @Resource
    private CategorySkillUserRepository categorySkillUserRepository;

    @Resource
    private CategorySkillUserMapper categorySkillUserMapper;

    @Override
    public Long createCategorySkillUser(CategorySkillUserCreateReqVO createReqVO) {
        // 插入
        CategorySkillUser categorySkillUser = categorySkillUserMapper.toEntity(createReqVO);
        categorySkillUserRepository.save(categorySkillUser);
        // 返回
        return categorySkillUser.getId();
    }

    @Override
    public void updateCategorySkillUser(CategorySkillUserUpdateReqVO updateReqVO) {
        // 校验存在
        validateCategorySkillUserExists(updateReqVO.getId());
        // 更新
        CategorySkillUser updateObj = categorySkillUserMapper.toEntity(updateReqVO);
        categorySkillUserRepository.save(updateObj);
    }

    @Override
    public void deleteCategorySkillUser(Long id) {
        // 校验存在
        validateCategorySkillUserExists(id);
        // 删除
        categorySkillUserRepository.deleteById(id);
    }

    private void validateCategorySkillUserExists(Long id) {
        categorySkillUserRepository.findById(id).orElseThrow(() -> exception(CATEGORY_SKILL_USER_NOT_EXISTS));
    }

    @Override
    public Optional<CategorySkillUser> getCategorySkillUser(Long id) {
        return categorySkillUserRepository.findById(id);
    }

    @Override
    public List<CategorySkillUser> getCategorySkillUserList(Collection<Long> ids) {
        return StreamSupport.stream(categorySkillUserRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<CategorySkillUser> getCategorySkillUserPage(CategorySkillUserPageReqVO pageReqVO, CategorySkillUserPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<CategorySkillUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), pageReqVO.getCategoryId()));
            }

            if(pageReqVO.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), pageReqVO.getUserId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<CategorySkillUser> page = categorySkillUserRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<CategorySkillUser> getCategorySkillUserList(CategorySkillUserExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<CategorySkillUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), exportReqVO.getCategoryId()));
            }

            if(exportReqVO.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), exportReqVO.getUserId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return categorySkillUserRepository.findAll(spec);
    }

    private Sort createSort(CategorySkillUserPageOrder order) {
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

        if (order.getUserId() != null) {
            orders.add(new Sort.Order(order.getUserId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "userId"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}