package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.taxrule.HrmSalaryTaxRuleSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 计税规则 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryTaxRuleService {

    /**
     * 创建计税规则
     *
     * @param createReqVO 创建信息
     * @return 计税规则编号
     */
    Long createSalaryTaxRule(@Valid HrmSalaryTaxRuleSaveReqVO createReqVO);

    /**
     * 更新计税规则
     *
     * @param updateReqVO 更新信息
     */
    void updateSalaryTaxRule(@Valid HrmSalaryTaxRuleSaveReqVO updateReqVO);

    /**
     * 删除计税规则
     *
     * @param id 计税规则编号
     */
    void deleteSalaryTaxRule(Long id);

    /**
     * 获得计税规则
     *
     * @param id 计税规则编号
     * @return 计税规则
     */
    HrmSalaryTaxRuleDO getSalaryTaxRule(Long id);

    /**
     * 获得计税规则列表
     *
     * @return 计税规则列表
     */
    List<HrmSalaryTaxRuleDO> getSalaryTaxRuleList();

    /**
     * 获得计税规则列表
     *
     * @param ids 计税规则编号集合
     * @return 计税规则列表
     */
    List<HrmSalaryTaxRuleDO> getSalaryTaxRuleList(Collection<Long> ids);

    /**
     * 获得计税规则 Map
     *
     * @param ids 计税规则编号集合
     * @return 计税规则 Map
     */
    default Map<Long, HrmSalaryTaxRuleDO> getSalaryTaxRuleMap(Collection<Long> ids) {
        return convertMap(getSalaryTaxRuleList(ids), HrmSalaryTaxRuleDO::getId);
    }

    /**
     * 校验计税规则存在
     *
     * @param id 计税规则编号
     * @return 计税规则
     */
    HrmSalaryTaxRuleDO validateSalaryTaxRuleExists(Long id);

}
