package cn.iocoder.yudao.module.hrm.controller.admin.insurance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeProjectRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;

@Tag(name = "管理后台 - HRM 社保方案")
@RestController
@RequestMapping("/hrm/insurance/scheme")
@Validated
public class HrmInsuranceSchemeController {

    @Resource
    private HrmInsuranceSchemeService insuranceSchemeService;
    @Resource
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @Resource
    private HrmInsuranceMonthEmployeeRecordService insuranceMonthEmployeeRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建社保方案")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:scheme:create')")
    public CommonResult<Long> createScheme(@Valid @RequestBody HrmInsuranceSchemeSaveReqVO reqVO) {
        return success(insuranceSchemeService.createScheme(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新社保方案")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:scheme:update')")
    public CommonResult<Boolean> updateScheme(@Valid @RequestBody HrmInsuranceSchemeSaveReqVO reqVO) {
        insuranceSchemeService.updateScheme(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除社保方案")
    @Parameter(name = "id", description = "方案编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:scheme:delete')")
    public CommonResult<Boolean> deleteScheme(@RequestParam("id") Long id) {
        insuranceSchemeService.deleteScheme(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得社保方案")
    @Parameter(name = "id", description = "社保方案编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:scheme:query')")
    public CommonResult<HrmInsuranceSchemeRespVO> getScheme(@RequestParam("id") Long id) {
        HrmInsuranceSchemeDO scheme = insuranceSchemeService.getScheme(id);
        return success(buildSchemeRespVO(scheme));
    }

    @GetMapping("/list")
    @Operation(summary = "获得社保方案列表")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:scheme:query')")
    public CommonResult<List<HrmInsuranceSchemeRespVO>> getSchemeList() {
        List<HrmInsuranceSchemeDO> schemes = insuranceSchemeService.getSchemeList();
        return success(buildSchemeRespVOList(schemes));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得社保方案精简列表")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:scheme:query')")
    public CommonResult<List<HrmInsuranceSchemeRespVO>> getSchemeSimpleList() {
        List<HrmInsuranceSchemeDO> list = insuranceSchemeService.getSchemeList();
        return success(BeanUtils.toBean(list, HrmInsuranceSchemeRespVO.class,
                scheme -> scheme.setAreaName(AreaUtils.format(scheme.getAreaId()))));
    }

    // ==================== 拼接 VO ====================

    private HrmInsuranceSchemeRespVO buildSchemeRespVO(HrmInsuranceSchemeDO scheme) {
        if (scheme == null) {
            return null;
        }
        return CollUtil.getFirst(buildSchemeRespVOList(Collections.singletonList(scheme)));
    }

    private List<HrmInsuranceSchemeRespVO> buildSchemeRespVOList(List<HrmInsuranceSchemeDO> schemes) {
        if (CollUtil.isEmpty(schemes)) {
            return Collections.emptyList();
        }
        // 1. 批量查询社保项目和使用人数
        Set<Long> schemeIds = convertSet(schemes, HrmInsuranceSchemeDO::getId);
        Map<Long, List<HrmInsuranceSchemeProjectDO>> projectMap =
                insuranceSchemeService.getSchemeProjectListMap(schemeIds);
        Map<Long, Long> useCountMap =
                insuranceEmployeeInfoService.getInsuranceEmployeeInfoCountMapBySchemeIds(schemeIds);
        Map<Long, Long> monthRecordCountMap = insuranceMonthEmployeeRecordService
                .getMonthEmployeeRecordCountMapBySchemeIds(schemeIds);

        // 2. 拼接响应
        return convertList(schemes, scheme -> {
            HrmInsuranceSchemeRespVO respVO = BeanUtils.toBean(scheme, HrmInsuranceSchemeRespVO.class);
            respVO.setAreaName(AreaUtils.format(respVO.getAreaId()));
            List<HrmInsuranceSchemeProjectRespVO> projects = BeanUtils.toBean(
                    projectMap.getOrDefault(scheme.getId(), Collections.emptyList()), HrmInsuranceSchemeProjectRespVO.class);
            respVO.setProjectList(projects);
            List<HrmInsuranceSchemeProjectRespVO> socialSecurityProjects = convertList(
                    projects, project -> project,
                    project -> HrmInsuranceProjectTypeEnum.isSocialSecurity(project.getType()));
            List<HrmInsuranceSchemeProjectRespVO> providentFundProjects = convertList(
                    projects, project -> project,
                    project -> HrmInsuranceProjectTypeEnum.isProvidentFund(project.getType()));
            respVO.setSocialSecurityProjectList(socialSecurityProjects)
                    .setProvidentFundProjectList(providentFundProjects);
            respVO.setPersonalInsuranceAmount(getSumValue(socialSecurityProjects,
                            HrmInsuranceSchemeProjectRespVO::getPersonalAmount, BigDecimal::add, BigDecimal.ZERO))
                    .setCorporateInsuranceAmount(getSumValue(socialSecurityProjects,
                            HrmInsuranceSchemeProjectRespVO::getCorporateAmount, BigDecimal::add, BigDecimal.ZERO))
                    .setPersonalProvidentFundAmount(getSumValue(providentFundProjects,
                            HrmInsuranceSchemeProjectRespVO::getPersonalAmount, BigDecimal::add, BigDecimal.ZERO))
                    .setCorporateProvidentFundAmount(getSumValue(providentFundProjects,
                            HrmInsuranceSchemeProjectRespVO::getCorporateAmount, BigDecimal::add, BigDecimal.ZERO))
                    .setUseCount(useCountMap.getOrDefault(scheme.getId(), 0L))
                    .setMonthRecordCount(monthRecordCountMap.getOrDefault(scheme.getId(), 0L));
            return respVO;
        });
    }

}
