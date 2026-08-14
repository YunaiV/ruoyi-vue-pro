package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HRM 薪资组 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryGroupService {

    /**
     * 创建薪资组
     *
     * @param createReqVO 创建信息
     * @return 薪资组编号
     */
    Long createSalaryGroup(@Valid HrmSalaryGroupSaveReqVO createReqVO);

    /**
     * 更新薪资组
     *
     * @param updateReqVO 更新信息
     */
    void updateSalaryGroup(@Valid HrmSalaryGroupSaveReqVO updateReqVO);

    /**
     * 删除薪资组
     *
     * @param id 薪资组编号
     */
    void deleteSalaryGroup(Long id);

    /**
     * 获得薪资组
     *
     * @param id 薪资组编号
     * @return 薪资组
     */
    HrmSalaryGroupDO getSalaryGroup(Long id);

    /**
     * 获得薪资组分页
     *
     * @param pageReqVO 分页查询
     * @return 薪资组分页
     */
    PageResult<HrmSalaryGroupDO> getSalaryGroupPage(HrmSalaryGroupPageReqVO pageReqVO);

    /**
     * 获得薪资组列表
     *
     * @return 薪资组列表
     */
    List<HrmSalaryGroupDO> getSalaryGroupList();

    /**
     * 获得员工适用的薪资组 Map
     *
     * 员工单独配置优先于部门配置，部门配置向下覆盖子部门
     *
     * @param employees 员工列表
     * @return 员工编号与薪资组的映射
     */
    Map<Long, HrmSalaryGroupDO> getEmployeeSalaryGroupMap(Collection<HrmEmployeeDO> employees);

    /**
     * 获得使用指定计税规则的薪资组数量
     *
     * @param taxRuleId 计税规则编号
     * @return 薪资组数量
     */
    default long getSalaryGroupCountByTaxRuleId(Long taxRuleId) {
        return getSalaryGroupCountMapByTaxRuleIds(Collections.singleton(taxRuleId))
                .getOrDefault(taxRuleId, 0L);
    }

    /**
     * 获得计税规则使用的薪资组数量 Map
     *
     * @param taxRuleIds 计税规则编号集合
     * @return 计税规则编号与薪资组数量的映射
     */
    Map<Long, Long> getSalaryGroupCountMapByTaxRuleIds(Collection<Long> taxRuleIds);

}
