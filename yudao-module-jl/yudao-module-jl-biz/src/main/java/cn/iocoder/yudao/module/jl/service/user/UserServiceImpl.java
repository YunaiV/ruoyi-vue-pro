package cn.iocoder.yudao.module.jl.service.user;

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
import cn.iocoder.yudao.module.jl.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.jl.entity.user.User;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.user.UserMapper;
import cn.iocoder.yudao.module.jl.repository.user.UserRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 用户信息 Service 实现类
 *
 */
@Service
@Validated
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserMapper userMapper;

    @Override
    public Long createUser(UserCreateReqVO createReqVO) {
        // 插入
        User user = userMapper.toEntity(createReqVO);
        userRepository.save(user);
        // 返回
        return user.getId();
    }

    @Override
    public void updateUser(UserUpdateReqVO updateReqVO) {
        // 校验存在
//        validateUserExists(updateReqVO.getId());
        // 更新
        User updateObj = userMapper.toEntity(updateReqVO);
        userRepository.save(updateObj);
    }

    @Override
    public void deleteUser(Long id) {
        // 校验存在
        validateUserExists(id);
        // 删除
        userRepository.deleteById(id);
    }

    private void validateUserExists(Long id) {
        userRepository.findById(id).orElseThrow(() -> exception(USER_NOT_EXISTS));
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getUserList(Collection<Long> ids) {
        return StreamSupport.stream(userRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<User> getUserPage(UserPageReqVO pageReqVO, UserPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getUsername() != null) {
                predicates.add(cb.like(root.get("username"), "%" + pageReqVO.getUsername() + "%"));
            }

            if(pageReqVO.getNickname() != null) {
                predicates.add(cb.like(root.get("nickname"), "%" + pageReqVO.getNickname() + "%"));
            }

            if(pageReqVO.getRemark() != null) {
                predicates.add(cb.equal(root.get("remark"), pageReqVO.getRemark()));
            }

            if(pageReqVO.getDeptId() != null) {
                predicates.add(cb.equal(root.get("deptId"), pageReqVO.getDeptId()));
            }

            if(pageReqVO.getPostIds() != null) {
                predicates.add(cb.equal(root.get("postIds"), pageReqVO.getPostIds()));
            }

            if(pageReqVO.getEmail() != null) {
                predicates.add(cb.equal(root.get("email"), pageReqVO.getEmail()));
            }

            if(pageReqVO.getMobile() != null) {
                predicates.add(cb.equal(root.get("mobile"), pageReqVO.getMobile()));
            }

            if(pageReqVO.getSex() != null) {
                predicates.add(cb.equal(root.get("sex"), pageReqVO.getSex()));
            }

            if(pageReqVO.getAvatar() != null) {
                predicates.add(cb.equal(root.get("avatar"), pageReqVO.getAvatar()));
            }

            if(pageReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), pageReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<User> page = userRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<User> getUserList(UserExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getUsername() != null) {
                predicates.add(cb.like(root.get("username"), "%" + exportReqVO.getUsername() + "%"));
            }

            if(exportReqVO.getNickname() != null) {
                predicates.add(cb.like(root.get("nickname"), "%" + exportReqVO.getNickname() + "%"));
            }

            if(exportReqVO.getRemark() != null) {
                predicates.add(cb.equal(root.get("remark"), exportReqVO.getRemark()));
            }

            if(exportReqVO.getDeptId() != null) {
                predicates.add(cb.equal(root.get("deptId"), exportReqVO.getDeptId()));
            }

            if(exportReqVO.getPostIds() != null) {
                predicates.add(cb.equal(root.get("postIds"), exportReqVO.getPostIds()));
            }

            if(exportReqVO.getEmail() != null) {
                predicates.add(cb.equal(root.get("email"), exportReqVO.getEmail()));
            }

            if(exportReqVO.getMobile() != null) {
                predicates.add(cb.equal(root.get("mobile"), exportReqVO.getMobile()));
            }

            if(exportReqVO.getSex() != null) {
                predicates.add(cb.equal(root.get("sex"), exportReqVO.getSex()));
            }

            if(exportReqVO.getAvatar() != null) {
                predicates.add(cb.equal(root.get("avatar"), exportReqVO.getAvatar()));
            }

            if(exportReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), exportReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return userRepository.findAll(spec);
    }

    private Sort createSort(UserPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getUsername() != null) {
            orders.add(new Sort.Order(order.getUsername().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "username"));
        }

        if (order.getNickname() != null) {
            orders.add(new Sort.Order(order.getNickname().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "nickname"));
        }

        if (order.getRemark() != null) {
            orders.add(new Sort.Order(order.getRemark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "remark"));
        }

        if (order.getDeptId() != null) {
            orders.add(new Sort.Order(order.getDeptId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "deptId"));
        }

        if (order.getPostIds() != null) {
            orders.add(new Sort.Order(order.getPostIds().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "postIds"));
        }

        if (order.getEmail() != null) {
            orders.add(new Sort.Order(order.getEmail().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "email"));
        }

        if (order.getMobile() != null) {
            orders.add(new Sort.Order(order.getMobile().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mobile"));
        }

        if (order.getSex() != null) {
            orders.add(new Sort.Order(order.getSex().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "sex"));
        }

        if (order.getAvatar() != null) {
            orders.add(new Sort.Order(order.getAvatar().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "avatar"));
        }

        if (order.getStatus() != null) {
            orders.add(new Sort.Order(order.getStatus().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "status"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}