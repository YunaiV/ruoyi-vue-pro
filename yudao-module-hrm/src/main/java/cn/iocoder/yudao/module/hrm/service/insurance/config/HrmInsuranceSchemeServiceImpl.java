package cn.iocoder.yudao.module.hrm.service.insurance.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeProjectSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.config.HrmInsuranceSchemeProjectMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.config.HrmInsuranceSchemeMapper;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceSchemeTypeEnum;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceMultiplyPercent;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceScale;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_AREA_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_PROJECT_EMPTY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_PROJECT_TYPE_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_USED;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_SCHEME_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_SCHEME_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_SCHEME_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_SCHEME_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_SCHEME_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_SCHEME_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_SCHEME_UPDATE_SUCCESS;

/**
 * HRM 社保方案 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmInsuranceSchemeServiceImpl implements HrmInsuranceSchemeService {

    @Resource
    private HrmInsuranceSchemeMapper insuranceSchemeMapper;
    @Resource
    private HrmInsuranceSchemeProjectMapper insuranceSchemeProjectMapper;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmInsuranceMonthEmployeeRecordService insuranceMonthEmployeeRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_SCHEME_TYPE, subType = HRM_INSURANCE_SCHEME_CREATE_SUB_TYPE,
            bizNo = "{{#insuranceScheme.id}}", success = HRM_INSURANCE_SCHEME_CREATE_SUCCESS)
    public Long createScheme(HrmInsuranceSchemeSaveReqVO reqVO) {
        // 1. 校验方案名称和项目
        validateNameUnique(null, reqVO.getName());
        validateSchemeProjects(reqVO.getProjectList());
        Integer cityAreaId = validateAndGetCityAreaId(reqVO.getAreaId());

        // 2.1 创建社保方案
        HrmInsuranceSchemeDO scheme = BeanUtils.toBean(reqVO, HrmInsuranceSchemeDO.class)
                .setAreaId(cityAreaId);
        insuranceSchemeMapper.insert(scheme);
        // 2.2 创建社保方案项目
        insuranceSchemeProjectMapper.insertBatch(convertList(reqVO.getProjectList(),
                project -> buildSchemeProjectDO(scheme.getId(), reqVO.getType(), project).setId(null)));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("insuranceScheme", scheme);
        return scheme.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_SCHEME_TYPE, subType = HRM_INSURANCE_SCHEME_UPDATE_SUB_TYPE,
            bizNo = "{{#insuranceScheme.id}}", success = HRM_INSURANCE_SCHEME_UPDATE_SUCCESS)
    public void updateScheme(HrmInsuranceSchemeSaveReqVO reqVO) {
        // 1. 校验社保方案
        HrmInsuranceSchemeDO oldScheme = validateSchemeExistsForUpdate(reqVO.getId());
        validateNameUnique(reqVO.getId(), reqVO.getName());
        validateSchemeProjects(reqVO.getProjectList());
        Integer cityAreaId = validateAndGetCityAreaId(reqVO.getAreaId());

        // 2.1 删除旧方案及项目，避免修改员工和月度记录已经使用的方案版本
        insuranceSchemeProjectMapper.deleteBySchemeId(oldScheme.getId());
        insuranceSchemeMapper.deleteById(oldScheme.getId());
        // 2.2 创建新方案及项目
        HrmInsuranceSchemeDO scheme = BeanUtils.toBean(reqVO, HrmInsuranceSchemeDO.class)
                .setId(null).setAreaId(cityAreaId);
        insuranceSchemeMapper.insert(scheme);
        insuranceSchemeProjectMapper.insertBatch(convertList(reqVO.getProjectList(),
                project -> buildSchemeProjectDO(scheme.getId(), reqVO.getType(), project).setId(null)));

        // 3. 将员工和月度记录迁移到新方案，历史月度金额及项目快照保持不变
        insuranceEmployeeInfoService.updateInsuranceEmployeeInfoSchemeIdBySchemeId(
                oldScheme.getId(), scheme.getId());
        insuranceMonthEmployeeRecordService.updateInsuranceMonthEmployeeRecordSchemeIdBySchemeId(
                oldScheme.getId(), scheme.getId());

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("insuranceScheme", scheme);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_SCHEME_TYPE, subType = HRM_INSURANCE_SCHEME_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = HRM_INSURANCE_SCHEME_DELETE_SUCCESS)
    public void deleteScheme(Long id) {
        // 1. 校验社保方案及使用情况
        HrmInsuranceSchemeDO scheme = validateSchemeExists(id);
        long usedByEmployee = insuranceEmployeeInfoService.getInsuranceEmployeeInfoCountBySchemeId(id);
        long usedByMonth = insuranceMonthEmployeeRecordService.getMonthEmployeeRecordCountBySchemeId(id);
        if (usedByEmployee > 0 || usedByMonth > 0) {
            throw exception(INSURANCE_SCHEME_USED);
        }

        // 2. 删除社保方案及项目
        insuranceSchemeProjectMapper.deleteBySchemeId(id);
        insuranceSchemeMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("insuranceScheme", scheme);
    }

    @Override
    public HrmInsuranceSchemeDO getScheme(Long id) {
        return insuranceSchemeMapper.selectById(id);
    }

    @Override
    public HrmInsuranceSchemeDO getSchemeByName(String name) {
        return insuranceSchemeMapper.selectByName(name);
    }

    @Override
    public HrmInsuranceSchemeDO validateSchemeExists(Long id) {
        HrmInsuranceSchemeDO scheme = insuranceSchemeMapper.selectById(id);
        if (scheme == null) {
            throw exception(INSURANCE_SCHEME_NOT_EXISTS);
        }
        return scheme;
    }

    @Override
    public List<HrmInsuranceSchemeDO> getSchemeList() {
        return insuranceSchemeMapper.selectListByIdDesc();
    }

    @Override
    public List<HrmInsuranceSchemeDO> getSchemeListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return insuranceSchemeMapper.selectByIds(ids);
    }

    @Override
    public List<HrmInsuranceSchemeDO> getSchemeListByAreaId(Integer areaId) {
        Integer cityAreaId = AreaUtils.getParentIdByType(areaId, AreaTypeEnum.CITY);
        return cityAreaId == null ? Collections.emptyList()
                : insuranceSchemeMapper.selectListByAreaId(cityAreaId);
    }

    @Override
    public List<HrmInsuranceSchemeProjectDO> getSchemeProjectList(Long schemeId) {
        return insuranceSchemeProjectMapper.selectListBySchemeId(schemeId);
    }

    @Override
    public Map<Long, List<HrmInsuranceSchemeProjectDO>> getSchemeProjectListMap(Collection<Long> schemeIds) {
        if (CollUtil.isEmpty(schemeIds)) {
            return Collections.emptyMap();
        }
        return convertMultiMap(insuranceSchemeProjectMapper.selectListBySchemeIds(schemeIds),
                HrmInsuranceSchemeProjectDO::getSchemeId);
    }

    /**
     * 校验社保方案名称是否重复
     *
     * @param id   社保方案编号
     * @param name 社保方案名称
     */
    private void validateNameUnique(Long id, String name) {
        HrmInsuranceSchemeDO scheme = insuranceSchemeMapper.selectByName(name);
        if (scheme != null && ObjectUtil.notEqual(scheme.getId(), id)) {
            throw exception(INSURANCE_SCHEME_NAME_DUPLICATE);
        }
    }

    /**
     * 校验社保方案项目的业务规则
     *
     * @param projects 社保方案项目列表
     */
    private void validateSchemeProjects(List<HrmInsuranceSchemeProjectSaveReqVO> projects) {
        // 1. 校验标准项目类型不可重复
        boolean hasSocialSecurityProject = false;
        Set<Integer> standardProjectTypes = new HashSet<>();
        for (HrmInsuranceSchemeProjectSaveReqVO project : projects) {
            HrmInsuranceProjectTypeEnum projectType = HrmInsuranceProjectTypeEnum.valueOf(project.getType());
            if (projectType == null) {
                throw exception(INSURANCE_DATA_ILLEGAL);
            }
            if (projectType.isSocialSecurity()) {
                hasSocialSecurityProject = true;
            }
            if (!projectType.isCustom() && !standardProjectTypes.add(projectType.getType())) {
                throw exception(INSURANCE_SCHEME_PROJECT_TYPE_DUPLICATE);
            }
        }
        // 2. 校验至少配置一个社保项目
        if (!hasSocialSecurityProject) {
            throw exception(INSURANCE_SCHEME_PROJECT_EMPTY);
        }
    }

    private HrmInsuranceSchemeDO validateSchemeExistsForUpdate(Long id) {
        HrmInsuranceSchemeDO scheme = insuranceSchemeMapper.selectByIdForUpdate(id);
        if (scheme == null) {
            throw exception(INSURANCE_SCHEME_NOT_EXISTS);
        }
        return scheme;
    }

    private Integer validateAndGetCityAreaId(Integer areaId) {
        Integer cityAreaId = AreaUtils.getParentIdByType(areaId, AreaTypeEnum.CITY);
        if (cityAreaId == null) {
            throw exception(INSURANCE_SCHEME_AREA_INVALID);
        }
        return cityAreaId;
    }

    /**
     * 构建社保方案项目
     *
     * @param schemeId 社保方案编号
     * @param type     社保方案类型
     * @param reqVO    社保方案项目
     * @return 社保方案项目
     */
    private HrmInsuranceSchemeProjectDO buildSchemeProjectDO(Long schemeId, Integer type,
                                                              HrmInsuranceSchemeProjectSaveReqVO reqVO) {
        // 1. 统一金额和比例精度
        HrmInsuranceSchemeProjectDO project = BeanUtils.toBean(reqVO, HrmInsuranceSchemeProjectDO.class)
                .setSchemeId(schemeId)
                .setBaseAmount(priceScale(reqVO.getBaseAmount()))
                .setCorporateRate(priceScale(reqVO.getCorporateRate()))
                .setPersonalRate(priceScale(reqVO.getPersonalRate()));
        // 2. 按比例方案自动计算金额，固定金额方案保留填写金额
        if (ObjectUtil.equal(type, HrmInsuranceSchemeTypeEnum.PROPORTION.getType())) {
            return project.setCorporateAmount(priceMultiplyPercent(project.getBaseAmount(), project.getCorporateRate()))
                    .setPersonalAmount(priceMultiplyPercent(project.getBaseAmount(), project.getPersonalRate()));
        }
        return project.setCorporateAmount(priceScale(project.getCorporateAmount()))
                .setPersonalAmount(priceScale(project.getPersonalAmount()));
    }

}
