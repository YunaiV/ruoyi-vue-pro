package cn.iocoder.yudao.module.jl.service.crm;

import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectConstractItemVO;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCustomerPlan;
import cn.iocoder.yudao.module.jl.entity.project.Project;
import cn.iocoder.yudao.module.jl.entity.project.ProjectConstract;
import cn.iocoder.yudao.module.jl.enums.ProjectTypeEnums;
import cn.iocoder.yudao.module.jl.enums.SalesLeadStatusEnums;
import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadCompetitorMapper;
import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadCustomerPlanMapper;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectConstractMapper;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectMapper;
import cn.iocoder.yudao.module.jl.repository.crm.CompetitorRepository;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadCompetitorRepository;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadCustomerPlanRepository;
import cn.iocoder.yudao.module.jl.repository.project.ProjectConstractRepository;
import cn.iocoder.yudao.module.jl.repository.project.ProjectRepository;
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
import cn.iocoder.yudao.module.jl.entity.crm.Saleslead;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadMapper;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 销售线索 Service 实现类
 *
 */
@Service
@Validated
public class SalesleadServiceImpl implements SalesleadService {

    @Resource
    private SalesleadRepository salesleadRepository;

    @Resource
    private SalesleadMapper salesleadMapper;

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private ProjectConstractRepository projectConstractRepository;

    @Resource
    private ProjectConstractMapper projectConstractMapper;

    @Resource
    private SalesleadCustomerPlanRepository salesleadCustomerPlanRepository;

    @Resource
    private SalesleadCustomerPlanMapper salesleadCustomerPlanMapper;

    @Resource
    private SalesleadCompetitorRepository salesleadCompetitorRepository;

    @Resource
    private SalesleadCompetitorMapper salesleadCompetitorMapper;

    @Override
    public Long createSaleslead(SalesleadCreateReqVO createReqVO) {
        // 插入
        Saleslead saleslead = salesleadMapper.toEntity(createReqVO);
        salesleadRepository.save(saleslead);
        // 返回
        return saleslead.getId();
    }

    @Override
    public void updateSaleslead(SalesleadUpdateReqVO updateReqVO) {

        if(updateReqVO.getId() != null) {
            // 校验存在
            validateSalesleadExists(updateReqVO.getId());
        }

        // 更新线索
        Saleslead updateObj = salesleadMapper.toEntity(updateReqVO);
        salesleadRepository.save(updateObj);
        Long salesleadId = updateObj.getId();

        // 更新竞争对手的报价
        // 删除原有的
        salesleadCompetitorRepository.deleteBySalesleadId(salesleadId);
        // 再插入
        List<SalesleadCompetitorItemVO> competitorQuotations = updateReqVO.getCompetitorQuotations();
        if(competitorQuotations != null && competitorQuotations.size() > 0) {
            // 遍历 competitorQuotations，将它的 salesleadId 字段设置为 updateObj.getId()
            competitorQuotations.forEach(competitorQuotation -> competitorQuotation.setSalesleadId(salesleadId));
            List<SalesleadCompetitor> quotations = salesleadCompetitorMapper.toEntityList(competitorQuotations);
            salesleadCompetitorRepository.saveAll(quotations);
        }

        // 更新客户方案
        // 删除原有的
        salesleadCustomerPlanRepository.deleteBySalesleadId(salesleadId);
        // 再插入
        List<SalesleadCustomerPlanItemVO> customerPlans = updateReqVO.getCustomerPlans();
        if(customerPlans != null && customerPlans.size() > 0) {
            // 遍历 customerPlans，将它的 salesleadId 字段设置为 updateObj.getId()
            customerPlans.forEach(customerPlan -> customerPlan.setSalesleadId(salesleadId));
            List<SalesleadCustomerPlan> plans = salesleadCustomerPlanMapper.toEntityList(customerPlans);
            salesleadCustomerPlanRepository.saveAll(plans);
        }

        // 转成项目的逻辑
        if(updateReqVO.getStatus().toString().equals(SalesLeadStatusEnums.CompletedTransaction.getStatus())) {
            // 1. 创建项目
            Project project = new Project();
            project.setSalesleadId(salesleadId);
            project.setCustomerId(updateReqVO.getCustomerId());
            project.setName(updateReqVO.getProjectName());
            project.setStage("0");
            project.setStatus("0");
            project.setType(ProjectTypeEnums.NormalProject.getStatus());
            project.setSalesId(updateObj.getCreator()); // 线索的销售人员 id
            projectRepository.save(project);

            // 2. 保存合同
            ProjectConstract contract = new ProjectConstract();

            // 遍历 updateReqVO.getProjectConstracts(), 创建合同
            List<ProjectConstractItemVO> projectConstracts = updateReqVO.getProjectConstracts();
            if(projectConstracts != null && projectConstracts.size() > 0) {
                // 遍历 projectConstracts，将它的 projectId 字段设置为 project.getId()
                projectConstracts.forEach(projectConstract -> {
                    projectConstract.setProjectId(project.getId());
                    projectConstract.setName(project.getName());
                });
                List<ProjectConstract> contracts = projectConstractMapper.toEntityList(projectConstracts);
                projectConstractRepository.saveAll(contracts);
            }
        } else if (updateReqVO.getStatus().toString().equals(SalesLeadStatusEnums.EmergencyProject.getStatus())) {
            // 临时应急项目
            // 1. 创建项目
            Project project = new Project();
            project.setSalesleadId(salesleadId);
            project.setCustomerId(updateReqVO.getCustomerId());
            project.setName(updateReqVO.getProjectName());
            project.setStage("0");
            project.setStatus("0");
            project.setType(ProjectTypeEnums.EmergencyProject.getStatus());
            project.setSalesId(updateObj.getCreator()); // 线索的销售人员 id
            projectRepository.save(project);
        }

    }

    @Override
    public void deleteSaleslead(Long id) {
        // 校验存在
        validateSalesleadExists(id);
        // 删除
        salesleadRepository.deleteById(id);
    }

    private void validateSalesleadExists(Long id) {
        salesleadRepository.findById(id).orElseThrow(() -> exception(SALESLEAD_NOT_EXISTS));
    }

    @Override
    public Optional<Saleslead> getSaleslead(Long id) {
        return salesleadRepository.findById(id);
    }

    @Override
    public List<Saleslead> getSalesleadList(Collection<Long> ids) {
        return StreamSupport.stream(salesleadRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Saleslead> getSalesleadPage(SalesleadPageReqVO pageReqVO, SalesleadPageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<Saleslead> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getSource() != null) {
                predicates.add(cb.equal(root.get("source"), pageReqVO.getSource()));
            }

            if(pageReqVO.getRequirement() != null) {
                predicates.add(cb.equal(root.get("requirement"), pageReqVO.getRequirement()));
            }

            if(pageReqVO.getBudget() != null) {
                predicates.add(cb.equal(root.get("budget"), pageReqVO.getBudget()));
            }

            if(Objects.equals(pageReqVO.getQuotationStatus(), "1")) {
                // 待报价的
                predicates.add(cb.isNull(root.get("quotation")));
            } else if (Objects.equals(pageReqVO.getQuotationStatus(), "2")) {
                // 已报价的
                predicates.add(cb.isNotNull(root.get("quotation")));
            }

            if(pageReqVO.getQuotation() != null) {
                predicates.add(cb.equal(root.get("quotation"), pageReqVO.getQuotation()));
            }

            if(pageReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), pageReqVO.getStatus()));
            }

            if(pageReqVO.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), pageReqVO.getCustomerId()));
            }

            if(pageReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), pageReqVO.getProjectId()));
            }

            if(pageReqVO.getBusinessType() != null) {
                predicates.add(cb.equal(root.get("businessType"), pageReqVO.getBusinessType()));
            }

            if(pageReqVO.getLostNote() != null) {
                predicates.add(cb.equal(root.get("lostNote"), pageReqVO.getLostNote()));
            }

            if(pageReqVO.getManagerId() != null) {
                predicates.add(cb.equal(root.get("managerId"), pageReqVO.getManagerId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<Saleslead> page = salesleadRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<Saleslead> getSalesleadList(SalesleadExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<Saleslead> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getSource() != null) {
                predicates.add(cb.equal(root.get("source"), exportReqVO.getSource()));
            }

            if(exportReqVO.getRequirement() != null) {
                predicates.add(cb.equal(root.get("requirement"), exportReqVO.getRequirement()));
            }

            if(exportReqVO.getBudget() != null) {
                predicates.add(cb.equal(root.get("budget"), exportReqVO.getBudget()));
            }

            if(exportReqVO.getQuotation() != null) {
                predicates.add(cb.equal(root.get("quotation"), exportReqVO.getQuotation()));
            }

            if(exportReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), exportReqVO.getStatus()));
            }

            if(exportReqVO.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), exportReqVO.getCustomerId()));
            }

            if(exportReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), exportReqVO.getProjectId()));
            }

            if(exportReqVO.getBusinessType() != null) {
                predicates.add(cb.equal(root.get("businessType"), exportReqVO.getBusinessType()));
            }

            if(exportReqVO.getLostNote() != null) {
                predicates.add(cb.equal(root.get("lostNote"), exportReqVO.getLostNote()));
            }

            if(exportReqVO.getManagerId() != null) {
                predicates.add(cb.equal(root.get("managerId"), exportReqVO.getManagerId()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return salesleadRepository.findAll(spec);
    }

    private Sort createSort(SalesleadPageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getSource() != null) {
            orders.add(new Sort.Order(order.getSource().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "source"));
        }

        if (order.getRequirement() != null) {
            orders.add(new Sort.Order(order.getRequirement().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "requirement"));
        }

        if (order.getBudget() != null) {
            orders.add(new Sort.Order(order.getBudget().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "budget"));
        }

        if (order.getQuotation() != null) {
            orders.add(new Sort.Order(order.getQuotation().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "quotation"));
        }

        if (order.getStatus() != null) {
            orders.add(new Sort.Order(order.getStatus().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "status"));
        }

        if (order.getCustomerId() != null) {
            orders.add(new Sort.Order(order.getCustomerId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "customerId"));
        }

        if (order.getProjectId() != null) {
            orders.add(new Sort.Order(order.getProjectId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "projectId"));
        }

        if (order.getBusinessType() != null) {
            orders.add(new Sort.Order(order.getBusinessType().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "businessType"));
        }

        if (order.getLostNote() != null) {
            orders.add(new Sort.Order(order.getLostNote().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "lostNote"));
        }

        if (order.getManagerId() != null) {
            orders.add(new Sort.Order(order.getManagerId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "managerId"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}