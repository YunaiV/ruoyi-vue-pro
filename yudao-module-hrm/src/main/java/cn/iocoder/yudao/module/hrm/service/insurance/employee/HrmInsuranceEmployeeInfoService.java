package cn.iocoder.yudao.module.hrm.service.insurance.employee;

import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.employeeinfo.HrmInsuranceEmployeeInfoSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.employee.HrmInsuranceEmployeeInfoDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 员工参保信息 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmInsuranceEmployeeInfoService {

    /**
     * 保存员工参保信息
     *
     * @param reqVO 员工参保信息
     * @return 员工参保信息编号
     */
    Long saveInsuranceEmployeeInfo(@Valid HrmInsuranceEmployeeInfoSaveReqVO reqVO);

    /**
     * 校验员工参保信息是否存在
     *
     * @param id 员工参保信息编号
     * @return 员工参保信息
     */
    HrmInsuranceEmployeeInfoDO validateInsuranceEmployeeInfoExists(Long id);

    /**
     * 获得员工参保信息
     *
     * @param employeeId 员工编号
     * @return 员工参保信息
     */
    HrmInsuranceEmployeeInfoDO getInsuranceEmployeeInfoByEmployeeId(Long employeeId);

    /**
     * 获得全部员工参保信息
     *
     * @return 员工参保信息列表
     */
    List<HrmInsuranceEmployeeInfoDO> getInsuranceEmployeeInfoList();

    /**
     * 获得员工参保信息列表
     *
     * @param employeeIds 员工编号集合
     * @return 员工参保信息列表
     */
    List<HrmInsuranceEmployeeInfoDO> getInsuranceEmployeeInfoList(Collection<Long> employeeIds);

    /**
     * 获得员工参保信息 Map
     *
     * @param employeeIds 员工编号集合
     * @return 员工编号与参保信息的映射
     */
    default Map<Long, HrmInsuranceEmployeeInfoDO> getInsuranceEmployeeInfoMap(Collection<Long> employeeIds) {
        return convertMap(getInsuranceEmployeeInfoList(employeeIds), HrmInsuranceEmployeeInfoDO::getEmployeeId);
    }

    /**
     * 获得使用指定社保方案的员工数量
     *
     * @param schemeId 社保方案编号
     * @return 员工数量
     */
    long getInsuranceEmployeeInfoCountBySchemeId(Long schemeId);

    /**
     * 获得各社保方案的使用员工数量
     *
     * @param schemeIds 社保方案编号集合
     * @return 社保方案编号与员工数量的映射
     */
    Map<Long, Long> getInsuranceEmployeeInfoCountMapBySchemeIds(Collection<Long> schemeIds);

    /**
     * 更新员工参保方案
     *
     * @param employeeId 员工编号
     * @param schemeId 社保方案编号
     */
    void updateEmployeeScheme(Long employeeId, Long schemeId);

    /**
     * 将指定社保方案下的员工参保信息迁移到新方案
     *
     * @param schemeId 旧社保方案编号
     * @param newSchemeId 新社保方案编号
     */
    void updateInsuranceEmployeeInfoSchemeIdBySchemeId(Long schemeId, Long newSchemeId);

    /**
     * 在员工尚未设置社保起缴月份时进行回填
     *
     * @param employeeId 员工编号
     * @param startMonth 社保起缴月份
     */
    void updateSocialSecurityStartMonthIfAbsent(Long employeeId, LocalDateTime startMonth);

}
