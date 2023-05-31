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
import cn.iocoder.yudao.module.jl.entity.crm.Customer;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.CustomerMapper;
import cn.iocoder.yudao.module.jl.repository.crm.CustomerRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 客户 Service 实现类
 *
 */
@Service
@Validated
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CustomerRepository customerRepository;

    @Resource
    private CustomerMapper customerMapper;

    @Override
    public Long createCustomer(CustomerCreateReqVO createReqVO) {
        // 插入
        Customer customer = customerMapper.toEntity(createReqVO);
        customerRepository.save(customer);
        // 返回
        return customer.getId();
    }

    @Override
    public void updateCustomer(CustomerUpdateReqVO updateReqVO) {
        // 校验存在
        validateCustomerExists(updateReqVO.getId());
        // 更新
        Customer updateObj = customerMapper.toEntity(updateReqVO);
        customerRepository.save(updateObj);
    }

    @Override
    public void deleteCustomer(Long id) {
        // 校验存在
        validateCustomerExists(id);
        // 删除
        customerRepository.deleteById(id);
    }

    private void validateCustomerExists(Long id) {
        customerRepository.findById(id).orElseThrow(() -> exception(CUSTOMER_NOT_EXISTS));
    }

    @Override
    public Optional<Customer> getCustomer(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> getCustomerList(Collection<Long> ids) {
        return StreamSupport.stream(customerRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Customer> getCustomerPage(CustomerPageReqVO pageReqVO, CustomerPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<Customer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getSource() != null) {
                predicates.add(cb.equal(root.get("source"), pageReqVO.getSource()));
            }

            if(pageReqVO.getPhone() != null) {
                predicates.add(cb.like(root.get("phone"), "%" + pageReqVO.getPhone() + "%"));
            }

            if(pageReqVO.getEmail() != null) {
                predicates.add(cb.equal(root.get("email"), pageReqVO.getEmail()));
            }

            if(pageReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), pageReqVO.getMark()));
            }

            if(pageReqVO.getWechat() != null) {
                predicates.add(cb.equal(root.get("wechat"), pageReqVO.getWechat()));
            }

            if(pageReqVO.getDoctorProfessionalRank() != null) {
                predicates.add(cb.equal(root.get("doctorProfessionalRank"), pageReqVO.getDoctorProfessionalRank()));
            }

            if(pageReqVO.getHospitalDepartment() != null) {
                predicates.add(cb.equal(root.get("hospitalDepartment"), pageReqVO.getHospitalDepartment()));
            }

            if(pageReqVO.getAcademicTitle() != null) {
                predicates.add(cb.equal(root.get("academicTitle"), pageReqVO.getAcademicTitle()));
            }

            if(pageReqVO.getAcademicCredential() != null) {
                predicates.add(cb.equal(root.get("academicCredential"), pageReqVO.getAcademicCredential()));
            }

            if(pageReqVO.getHospitalId() != null) {
                predicates.add(cb.equal(root.get("hospitalId"), pageReqVO.getHospitalId()));
            }

            if(pageReqVO.getUniversityId() != null) {
                predicates.add(cb.equal(root.get("universityId"), pageReqVO.getUniversityId()));
            }

            if(pageReqVO.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), pageReqVO.getCompanyId()));
            }

            if(pageReqVO.getProvince() != null) {
                predicates.add(cb.equal(root.get("province"), pageReqVO.getProvince()));
            }

            if(pageReqVO.getCity() != null) {
                predicates.add(cb.equal(root.get("city"), pageReqVO.getCity()));
            }

            if(pageReqVO.getArea() != null) {
                predicates.add(cb.equal(root.get("area"), pageReqVO.getArea()));
            }

            if(pageReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), pageReqVO.getType()));
            }

            if(pageReqVO.getDealCount() != null) {
                predicates.add(cb.equal(root.get("dealCount"), pageReqVO.getDealCount()));
            }

            if(pageReqVO.getDealTotalAmount() != null) {
                predicates.add(cb.equal(root.get("dealTotalAmount"), pageReqVO.getDealTotalAmount()));
            }

            if(pageReqVO.getArrears() != null) {
                predicates.add(cb.equal(root.get("arrears"), pageReqVO.getArrears()));
            }

            if(pageReqVO.getLastFollowupTime() != null) {
                predicates.add(cb.between(root.get("lastFollowupTime"), pageReqVO.getLastFollowupTime()[0], pageReqVO.getLastFollowupTime()[1]));
            } 
            if(pageReqVO.getSalesId() != null) {
                predicates.add(cb.equal(root.get("salesId"), pageReqVO.getSalesId()));
            }

            if(pageReqVO.getLastFollowupId() != null) {
                predicates.add(cb.equal(root.get("lastFollowupId"), pageReqVO.getLastFollowupId()));
            }

            if(pageReqVO.getLastSalesleadId() != null) {
                predicates.add(cb.equal(root.get("lastSalesleadId"), pageReqVO.getLastSalesleadId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<Customer> page = customerRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<Customer> getCustomerList(CustomerExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<Customer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getSource() != null) {
                predicates.add(cb.equal(root.get("source"), exportReqVO.getSource()));
            }

            if(exportReqVO.getPhone() != null) {
                predicates.add(cb.like(root.get("phone"), "%" + exportReqVO.getPhone() + "%"));
            }

            if(exportReqVO.getEmail() != null) {
                predicates.add(cb.equal(root.get("email"), exportReqVO.getEmail()));
            }

            if(exportReqVO.getMark() != null) {
                predicates.add(cb.equal(root.get("mark"), exportReqVO.getMark()));
            }

            if(exportReqVO.getWechat() != null) {
                predicates.add(cb.equal(root.get("wechat"), exportReqVO.getWechat()));
            }

            if(exportReqVO.getDoctorProfessionalRank() != null) {
                predicates.add(cb.equal(root.get("doctorProfessionalRank"), exportReqVO.getDoctorProfessionalRank()));
            }

            if(exportReqVO.getHospitalDepartment() != null) {
                predicates.add(cb.equal(root.get("hospitalDepartment"), exportReqVO.getHospitalDepartment()));
            }

            if(exportReqVO.getAcademicTitle() != null) {
                predicates.add(cb.equal(root.get("academicTitle"), exportReqVO.getAcademicTitle()));
            }

            if(exportReqVO.getAcademicCredential() != null) {
                predicates.add(cb.equal(root.get("academicCredential"), exportReqVO.getAcademicCredential()));
            }

            if(exportReqVO.getHospitalId() != null) {
                predicates.add(cb.equal(root.get("hospitalId"), exportReqVO.getHospitalId()));
            }

            if(exportReqVO.getUniversityId() != null) {
                predicates.add(cb.equal(root.get("universityId"), exportReqVO.getUniversityId()));
            }

            if(exportReqVO.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), exportReqVO.getCompanyId()));
            }

            if(exportReqVO.getProvince() != null) {
                predicates.add(cb.equal(root.get("province"), exportReqVO.getProvince()));
            }

            if(exportReqVO.getCity() != null) {
                predicates.add(cb.equal(root.get("city"), exportReqVO.getCity()));
            }

            if(exportReqVO.getArea() != null) {
                predicates.add(cb.equal(root.get("area"), exportReqVO.getArea()));
            }

            if(exportReqVO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), exportReqVO.getType()));
            }

            if(exportReqVO.getDealCount() != null) {
                predicates.add(cb.equal(root.get("dealCount"), exportReqVO.getDealCount()));
            }

            if(exportReqVO.getDealTotalAmount() != null) {
                predicates.add(cb.equal(root.get("dealTotalAmount"), exportReqVO.getDealTotalAmount()));
            }

            if(exportReqVO.getArrears() != null) {
                predicates.add(cb.equal(root.get("arrears"), exportReqVO.getArrears()));
            }

            if(exportReqVO.getLastFollowupTime() != null) {
                predicates.add(cb.between(root.get("lastFollowupTime"), exportReqVO.getLastFollowupTime()[0], exportReqVO.getLastFollowupTime()[1]));
            } 
            if(exportReqVO.getSalesId() != null) {
                predicates.add(cb.equal(root.get("salesId"), exportReqVO.getSalesId()));
            }

            if(exportReqVO.getLastFollowupId() != null) {
                predicates.add(cb.equal(root.get("lastFollowupId"), exportReqVO.getLastFollowupId()));
            }

            if(exportReqVO.getLastSalesleadId() != null) {
                predicates.add(cb.equal(root.get("lastSalesleadId"), exportReqVO.getLastSalesleadId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return customerRepository.findAll(spec);
    }

    private Sort createSort(CustomerPageOrder order) {
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

        if (order.getSource() != null) {
            orders.add(new Sort.Order(order.getSource().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "source"));
        }

        if (order.getPhone() != null) {
            orders.add(new Sort.Order(order.getPhone().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "phone"));
        }

        if (order.getEmail() != null) {
            orders.add(new Sort.Order(order.getEmail().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "email"));
        }

        if (order.getMark() != null) {
            orders.add(new Sort.Order(order.getMark().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "mark"));
        }

        if (order.getWechat() != null) {
            orders.add(new Sort.Order(order.getWechat().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "wechat"));
        }

        if (order.getDoctorProfessionalRank() != null) {
            orders.add(new Sort.Order(order.getDoctorProfessionalRank().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "doctorProfessionalRank"));
        }

        if (order.getHospitalDepartment() != null) {
            orders.add(new Sort.Order(order.getHospitalDepartment().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "hospitalDepartment"));
        }

        if (order.getAcademicTitle() != null) {
            orders.add(new Sort.Order(order.getAcademicTitle().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "academicTitle"));
        }

        if (order.getAcademicCredential() != null) {
            orders.add(new Sort.Order(order.getAcademicCredential().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "academicCredential"));
        }

        if (order.getHospitalId() != null) {
            orders.add(new Sort.Order(order.getHospitalId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "hospitalId"));
        }

        if (order.getUniversityId() != null) {
            orders.add(new Sort.Order(order.getUniversityId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "universityId"));
        }

        if (order.getCompanyId() != null) {
            orders.add(new Sort.Order(order.getCompanyId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "companyId"));
        }

        if (order.getProvince() != null) {
            orders.add(new Sort.Order(order.getProvince().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "province"));
        }

        if (order.getCity() != null) {
            orders.add(new Sort.Order(order.getCity().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "city"));
        }

        if (order.getArea() != null) {
            orders.add(new Sort.Order(order.getArea().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "area"));
        }

        if (order.getType() != null) {
            orders.add(new Sort.Order(order.getType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "type"));
        }

        if (order.getDealCount() != null) {
            orders.add(new Sort.Order(order.getDealCount().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "dealCount"));
        }

        if (order.getDealTotalAmount() != null) {
            orders.add(new Sort.Order(order.getDealTotalAmount().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "dealTotalAmount"));
        }

        if (order.getArrears() != null) {
            orders.add(new Sort.Order(order.getArrears().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "arrears"));
        }

        if (order.getLastFollowupTime() != null) {
            orders.add(new Sort.Order(order.getLastFollowupTime().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "lastFollowupTime"));
        }

        if (order.getSalesId() != null) {
            orders.add(new Sort.Order(order.getSalesId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "salesId"));
        }

        if (order.getLastFollowupId() != null) {
            orders.add(new Sort.Order(order.getLastFollowupId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "lastFollowupId"));
        }

        if (order.getLastSalesleadId() != null) {
            orders.add(new Sort.Order(order.getLastSalesleadId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "lastSalesleadId"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}