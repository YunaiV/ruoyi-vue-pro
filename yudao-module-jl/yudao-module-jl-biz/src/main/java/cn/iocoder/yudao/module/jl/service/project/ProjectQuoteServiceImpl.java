package cn.iocoder.yudao.module.jl.service.project;

import cn.iocoder.yudao.module.jl.entity.project.ProjectCategory;
import cn.iocoder.yudao.module.jl.entity.project.ProjectChargeitem;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSupply;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectCategoryMapper;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectChargeitemMapper;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectSupplyMapper;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadRepository;
import cn.iocoder.yudao.module.jl.repository.laboratory.CategoryRepository;
import cn.iocoder.yudao.module.jl.repository.project.ProjectCategoryRepository;
import cn.iocoder.yudao.module.jl.repository.project.ProjectChargeitemRepository;
import cn.iocoder.yudao.module.jl.repository.project.ProjectSupplyRepository;
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
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.project.ProjectQuoteMapper;
import cn.iocoder.yudao.module.jl.repository.project.ProjectQuoteRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 项目报价 Service 实现类
 *
 */
@Service
@Validated
public class ProjectQuoteServiceImpl implements ProjectQuoteService {
    @Resource
    private SalesleadRepository salesleadRepository;

    @Resource
    private ProjectQuoteRepository projectQuoteRepository;

    @Resource
    private ProjectQuoteMapper projectQuoteMapper;

    @Resource
    private ProjectCategoryRepository projectCategoryRepository;

    @Resource
    private ProjectCategoryMapper projectCategoryMapper;

    @Resource
    private ProjectSupplyRepository projectSupplyRepository;

    @Resource
    private ProjectSupplyMapper projectSupplyMapper;

    @Resource
    private ProjectChargeitemRepository projectChargeitemRepository;

    @Resource
    private ProjectChargeitemMapper projectChargeitemMapper;


    @Override
    public Long createProjectQuote(ProjectQuoteCreateReqVO createReqVO) {
        // 插入
        ProjectQuote projectQuote = projectQuoteMapper.toEntity(createReqVO);
        projectQuoteRepository.save(projectQuote);
        // 返回
        return projectQuote.getId();
    }

    /**
     * 全量保存项目报价
     * @param saveReqVO
     * @return
     */
    @Override
    public Long saveProjectQuote(ProjectQuoteSaveReqVO saveReqVO) {
        // 如果提供了 quoteId ，则更新。否则，创建
        Long quoteId;
        if (saveReqVO.getId() != null) {
            quoteId = saveReqVO.getId();
            // 校验存在
            validateProjectQuoteExists(saveReqVO.getId());
            // 更新
            ProjectQuote updateObj = projectQuoteMapper.toEntity(saveReqVO);
            projectQuoteRepository.save(updateObj);
        } else {
            // 创建
            ProjectQuote projectQuote = projectQuoteMapper.toEntity(saveReqVO);
            projectQuoteRepository.save(projectQuote);

            // 更新销售线索的报价内容
            salesleadRepository.updateQuotationById(projectQuote.getSalesleadId(), projectQuote.getId());

            quoteId = projectQuote.getId();
        }

        List<ProjectCategoryWithSupplyAndChargeItemVO> categoryList = saveReqVO.getCategoryList();
        if(categoryList != null && categoryList.size() >= 1) {
            List<ProjectCategory> categories = projectCategoryRepository.getByQuoteId(quoteId);
            // 获取 categories 里的 id
            List<Long> categoryIds = categories.stream().map(ProjectCategory::getId).collect(Collectors.toList());
            // 删除原来的
            projectCategoryRepository.deleteByQuoteId(quoteId);
            projectSupplyRepository.deleteByProjectCategoryIdIn(categoryIds);
            projectChargeitemRepository.deleteByProjectCategoryIdIn(categoryIds);

            // 保存新的
            for (int i = 0; i < categoryList.size(); i++) {
                // 保存实验名目
                ProjectCategoryWithSupplyAndChargeItemVO category = categoryList.get(i);
                category.setCategoryType("quote");
                category.setQuoteId(quoteId);
                category.setType("1");
                ProjectCategory categoryDo = projectCategoryMapper.toEntity(category);
                projectCategoryRepository.save(categoryDo);

                // 保存收费项
                List<ProjectChargeitemSubClass> chargetItemList = category.getChargeList();
                if(chargetItemList != null && chargetItemList.size() >= 1) {
                    List<ProjectChargeitemSubClass> projectChargeitemList = chargetItemList.stream().map(chargeItem -> {
                        chargeItem.setProjectCategoryId(categoryDo.getId());
                        chargeItem.setCategoryId(categoryDo.getCategoryId());
                        return chargeItem;
                    }).collect(Collectors.toList());

                    List<ProjectChargeitem> projectChargeitems = projectChargeitemMapper.toEntity(projectChargeitemList);
                    projectChargeitemRepository.saveAll(projectChargeitems);
                }

                // 保存物资项
                List<ProjectSupplySubClass> supplyList = category.getSupplyList();
                if(supplyList != null && supplyList.size() >= 1) {
                    List<ProjectSupplySubClass> projectSupplyList = supplyList.stream().map(supply -> {
                        supply.setProjectCategoryId(categoryDo.getId());
                        supply.setCategoryId(categoryDo.getCategoryId());
                        return supply;
                    }).collect(Collectors.toList());

                    List<ProjectSupply> projectSupplies = projectSupplyMapper.toEntity(projectSupplyList);
                    projectSupplyRepository.saveAll(projectSupplies);
                }
            }
        }

        return quoteId;
    }

    @Override
    public void updateProjectQuote(ProjectQuoteUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectQuoteExists(updateReqVO.getId());
        // 更新
        ProjectQuote updateObj = projectQuoteMapper.toEntity(updateReqVO);
        projectQuoteRepository.save(updateObj);
    }

    @Override
    public void deleteProjectQuote(Long id) {
        // 校验存在
        validateProjectQuoteExists(id);
        // 删除
        projectQuoteRepository.deleteById(id);
    }

    private void validateProjectQuoteExists(Long id) {
        projectQuoteRepository.findById(id).orElseThrow(() -> exception(PROJECT_QUOTE_NOT_EXISTS));
    }

    @Override
    public Optional<ProjectQuote> getProjectQuote(Long id) {
        return projectQuoteRepository.findById(id);
    }

    @Override
    public List<ProjectQuote> getProjectQuoteList(Collection<Long> ids) {
        return StreamSupport.stream(projectQuoteRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProjectQuote> getProjectQuotePage(ProjectQuotePageReqVO pageReqVO, ProjectQuotePageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<ProjectQuote> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), pageReqVO.getSalesleadId()));
            }

            if(pageReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), pageReqVO.getProjectId()));
            }

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getReportUrl() != null) {
                predicates.add(cb.equal(root.get("reportUrl"), pageReqVO.getReportUrl()));
            }

            if(pageReqVO.getDiscount() != null) {
                predicates.add(cb.equal(root.get("discount"), pageReqVO.getDiscount()));
            }

            if(pageReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), pageReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ProjectQuote> page = projectQuoteRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<ProjectQuote> getProjectQuoteList(ProjectQuoteExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<ProjectQuote> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getSalesleadId() != null) {
                predicates.add(cb.equal(root.get("salesleadId"), exportReqVO.getSalesleadId()));
            }

            if(exportReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), exportReqVO.getProjectId()));
            }

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getReportUrl() != null) {
                predicates.add(cb.equal(root.get("reportUrl"), exportReqVO.getReportUrl()));
            }

            if(exportReqVO.getDiscount() != null) {
                predicates.add(cb.equal(root.get("discount"), exportReqVO.getDiscount()));
            }

            if(exportReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), exportReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return projectQuoteRepository.findAll(spec);
    }

    private Sort createSort(ProjectQuotePageOrder order) {
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

        if (order.getProjectId() != null) {
            orders.add(new Sort.Order(order.getProjectId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "projectId"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getReportUrl() != null) {
            orders.add(new Sort.Order(order.getReportUrl().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "reportUrl"));
        }

        if (order.getDiscount() != null) {
            orders.add(new Sort.Order(order.getDiscount().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "discount"));
        }

        if (order.getStatus() != null) {
            orders.add(new Sort.Order(order.getStatus().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "status"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}