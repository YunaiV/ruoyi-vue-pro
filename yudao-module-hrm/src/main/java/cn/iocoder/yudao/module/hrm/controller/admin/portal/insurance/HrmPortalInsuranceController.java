package cn.iocoder.yudao.module.hrm.controller.admin.portal.insurance;

import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.insurance.vo.HrmPortalInsuranceRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordService;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_EMP_RECORD_NOT_EXISTS;

@Tag(name = "管理后台 - HRM 员工端社保")
@RestController
@RequestMapping("/hrm/portal/insurance/record")
@Validated
public class HrmPortalInsuranceController {

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmInsuranceMonthEmployeeRecordService insuranceMonthEmployeeRecordService;
    @Resource
    private HrmInsuranceSchemeService insuranceSchemeService;

    @GetMapping("/list")
    @Operation(summary = "获得我的社保记录")
    @Parameter(name = "year", description = "年份", example = "2026")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmPortalInsuranceRecordRespVO>> getInsuranceRecordList(
            @RequestParam(value = "year", required = false) Integer year) {
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        List<HrmInsuranceMonthEmployeeRecordDO> records =
                insuranceMonthEmployeeRecordService.getMonthEmployeeRecordListByEmployeeIdAndYear(
                        employee.getId(), year);
        // 拼接 VO
        return success(buildInsuranceRecordRespVOList(records));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的社保记录详情")
    @Parameter(name = "id", description = "社保记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmPortalInsuranceRecordRespVO> getInsuranceRecord(@RequestParam("id") Long id) {
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        HrmInsuranceMonthEmployeeRecordDO record = insuranceMonthEmployeeRecordService.getMonthEmployeeRecord(id);
        if (record == null || notEqual(record.getEmployeeId(), employee.getId())) {
            throw exception(INSURANCE_MONTH_EMP_RECORD_NOT_EXISTS);
        }
        // 拼接 VO
        return success(buildInsuranceRecordRespVO(
                record, insuranceSchemeService.getScheme(record.getSchemeId()), true));
    }

    // ==================== 拼接 VO ====================

    /**
     * 构建社保记录响应列表
     *
     * @param records 社保记录列表
     * @return 社保记录响应列表
     */
    private List<HrmPortalInsuranceRecordRespVO> buildInsuranceRecordRespVOList(
            List<HrmInsuranceMonthEmployeeRecordDO> records) {
        Map<Long, HrmInsuranceSchemeDO> schemeMap = insuranceSchemeService.getSchemeMap(
                convertSet(records, HrmInsuranceMonthEmployeeRecordDO::getSchemeId));
        return convertList(records, record ->
                buildInsuranceRecordRespVO(record, schemeMap.get(record.getSchemeId()), false));
    }

    /**
     * 构建社保记录响应
     *
     * @param record 社保记录
     * @param scheme 社保方案
     * @param includeProjects 是否返回社保项目
     * @return 社保记录响应
     */
    private HrmPortalInsuranceRecordRespVO buildInsuranceRecordRespVO(
            HrmInsuranceMonthEmployeeRecordDO record, HrmInsuranceSchemeDO scheme,
            boolean includeProjects) {
        HrmPortalInsuranceRecordRespVO respVO = BeanUtils.toBean(record, HrmPortalInsuranceRecordRespVO.class);
        if (scheme != null) {
            respVO.setSchemeName(scheme.getName()).setSchemeType(scheme.getType())
                    .setSchemeCity(AreaUtils.format(scheme.getAreaId()));
        }
        if (BooleanUtil.isFalse(includeProjects)) {
            respVO.setProjects(null);
        }
        return respVO;
    }

}
