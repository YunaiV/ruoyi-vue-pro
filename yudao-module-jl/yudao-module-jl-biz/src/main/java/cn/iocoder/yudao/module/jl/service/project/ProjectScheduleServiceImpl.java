package cn.iocoder.yudao.module.jl.service.project;

import cn.iocoder.yudao.module.jl.entity.project.*;
import cn.iocoder.yudao.module.jl.mapper.project.*;
import cn.iocoder.yudao.module.jl.repository.crm.SalesleadRepository;
import cn.iocoder.yudao.module.jl.repository.project.*;
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
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 项目安排单 Service 实现类
 *
 */
@Service
@Validated
public class ProjectScheduleServiceImpl implements ProjectScheduleService {

    @Resource
    private ProjectScheduleRepository projectScheduleRepository;

    @Resource
    private ProjectScheduleMapper projectScheduleMapper;

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
    public Long createProjectSchedule(ProjectScheduleCreateReqVO createReqVO) {
        // 插入
        ProjectSchedule projectSchedule = projectScheduleMapper.toEntity(createReqVO);
        projectScheduleRepository.save(projectSchedule);
        // 返回
        return projectSchedule.getId();
    }

    /**
     * @param saveReqVO
     * @return
     */
    @Override
    public Long saveProjectSchedule(ProjectScheduleSaveReqVO saveReqVO) {
        // 如果提供了 scheduleId ，则更新。否则，创建
        Long scheduleId;
        if (saveReqVO.getId() != null) {
            scheduleId = saveReqVO.getId();
            // 校验存在
            validateProjectScheduleExists(saveReqVO.getId());
            // 更新
            ProjectSchedule updateObj = projectScheduleMapper.toEntity(saveReqVO);
            projectScheduleRepository.save(updateObj);
        } else {
            // 创建
            ProjectSchedule projectSchedule = projectScheduleMapper.toEntity(saveReqVO);
            projectScheduleRepository.save(projectSchedule);

            scheduleId = projectSchedule.getId();
        }

        List<ProjectCategoryWithSupplyAndChargeItemVO> categoryList = saveReqVO.getCategoryList();
        if(categoryList != null && categoryList.size() >= 1) {
            List<ProjectCategory> categories = projectCategoryRepository.findByScheduleIdOrderByIdAsc(scheduleId);
            // 获取 categories 里的 id
            List<Long> categoryIds = categories.stream().map(ProjectCategory::getId).collect(Collectors.toList());
            // 删除原来的
            projectCategoryRepository.deleteByScheduleId(scheduleId);
            projectSupplyRepository.deleteByProjectCategoryIdIn(categoryIds);
            projectChargeitemRepository.deleteByProjectCategoryIdIn(categoryIds);

            // 保存新的
            for (int i = 0; i < categoryList.size(); i++) {
                // 保存实验名目
                ProjectCategoryWithSupplyAndChargeItemVO category = categoryList.get(i);
                category.setCategoryType("schedule");
                category.setScheduleId(scheduleId);
                category.setType("1");
                ProjectCategory categoryDo = projectCategoryMapper.toEntity(category);
                projectCategoryRepository.save(categoryDo);

                // 保存收费项
                List<ProjectChargeitemSubClass> chargetItemList = category.getChargeItemList();
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
        return scheduleId;
    }

    @Override
    public void updateProjectSchedule(ProjectScheduleUpdateReqVO updateReqVO) {
        // 校验存在
        validateProjectScheduleExists(updateReqVO.getId());
        // 更新
        ProjectSchedule updateObj = projectScheduleMapper.toEntity(updateReqVO);
        projectScheduleRepository.save(updateObj);
    }

    @Override
    public void deleteProjectSchedule(Long id) {
        // 校验存在
        validateProjectScheduleExists(id);
        // 删除
        projectScheduleRepository.deleteById(id);
    }

    private void validateProjectScheduleExists(Long id) {
        projectScheduleRepository.findById(id).orElseThrow(() -> exception(PROJECT_SCHEDULE_NOT_EXISTS));
    }

    @Override
    public Optional<ProjectSchedule> getProjectSchedule(Long id) {
        return projectScheduleRepository.findById(id);
    }

    @Override
    public List<ProjectSchedule> getProjectScheduleList(Collection<Long> ids) {
        return StreamSupport.stream(projectScheduleRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProjectSchedule> getProjectSchedulePage(ProjectSchedulePageReqVO pageReqVO, ProjectSchedulePageOrder orderV0) {
        // 创建 Sort 对象
        Sort sort = createSort(orderV0);

        // 创建 Pageable 对象
        Pageable pageable = PageRequest.of(pageReqVO.getPageNo() - 1, pageReqVO.getPageSize(), sort);

        // 创建 Specification
        Specification<ProjectSchedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(pageReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), pageReqVO.getProjectId()));
            }

            if(pageReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + pageReqVO.getName() + "%"));
            }

            if(pageReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), pageReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ProjectSchedule> page = projectScheduleRepository.findAll(spec, pageable);

        // 转换为 PageResult 并返回
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    public List<ProjectSchedule> getProjectScheduleList(ProjectScheduleExportReqVO exportReqVO) {
        // 创建 Specification
        Specification<ProjectSchedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(exportReqVO.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), exportReqVO.getProjectId()));
            }

            if(exportReqVO.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + exportReqVO.getName() + "%"));
            }

            if(exportReqVO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), exportReqVO.getStatus()));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        return projectScheduleRepository.findAll(spec);
    }

    private Sort createSort(ProjectSchedulePageOrder order) {
        List<Sort.Order> orders = new ArrayList<>();

        // 根据 order 中的每个属性创建一个排序规则
        // 注意，这里假设 order 中的每个属性都是 String 类型，代表排序的方向（"asc" 或 "desc"）
        // 如果实际情况不同，你可能需要对这部分代码进行调整

        if (order.getId() != null) {
            orders.add(new Sort.Order(order.getId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));
        }

        if (order.getProjectId() != null) {
            orders.add(new Sort.Order(order.getProjectId().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "projectId"));
        }

        if (order.getName() != null) {
            orders.add(new Sort.Order(order.getName().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "name"));
        }

        if (order.getStatus() != null) {
            orders.add(new Sort.Order(order.getStatus().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "status"));
        }


        // 创建 Sort 对象
        return Sort.by(orders);
    }
}