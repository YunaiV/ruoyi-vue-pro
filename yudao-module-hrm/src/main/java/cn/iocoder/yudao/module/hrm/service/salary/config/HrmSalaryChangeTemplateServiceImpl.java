package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changetemplate.HrmSalaryChangeOptionVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changetemplate.HrmSalaryChangeTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryChangeTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryChangeTemplateDO.Option;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryChangeTemplateMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_TEMPLATE_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_TEMPLATE_OPTION_INVALID;

/**
 * HRM 调薪模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalaryChangeTemplateServiceImpl implements HrmSalaryChangeTemplateService {

    @Resource
    private HrmSalaryChangeTemplateMapper salaryChangeTemplateMapper;
    @Resource
    private HrmSalaryOptionService salaryOptionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSalaryChangeTemplate(HrmSalaryChangeTemplateSaveReqVO createReqVO) {
        // 1. 校验调薪项
        List<Option> options = buildSalaryChangeOptions(createReqVO.getOptions());

        // 2. 清理原默认模板
        if (Boolean.TRUE.equals(createReqVO.getDefaultStatus())) {
            clearSalaryChangeTemplateDefault(null);
        }

        // 3. 创建调薪模板
        HrmSalaryChangeTemplateDO template = new HrmSalaryChangeTemplateDO()
                .setName(createReqVO.getName()).setDefaultStatus(createReqVO.getDefaultStatus())
                .setOptions(options);
        salaryChangeTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSalaryChangeTemplate(HrmSalaryChangeTemplateSaveReqVO updateReqVO) {
        // 1.1 校验调薪模板存在
        validateSalaryChangeTemplateExists(updateReqVO.getId());
        // 1.2 校验调薪项
        List<Option> options = buildSalaryChangeOptions(updateReqVO.getOptions());

        // 2.1 清理原默认模板
        if (Boolean.TRUE.equals(updateReqVO.getDefaultStatus())) {
            clearSalaryChangeTemplateDefault(updateReqVO.getId());
        }
        // 2.2 更新调薪模板
        HrmSalaryChangeTemplateDO updateObj = new HrmSalaryChangeTemplateDO()
                .setId(updateReqVO.getId()).setName(updateReqVO.getName())
                .setDefaultStatus(updateReqVO.getDefaultStatus()).setOptions(options);
        salaryChangeTemplateMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSalaryChangeTemplate(Long id) {
        // 1. 校验调薪模板存在且不是默认模板
        HrmSalaryChangeTemplateDO template = validateSalaryChangeTemplateExists(id);
        if (Boolean.TRUE.equals(template.getDefaultStatus())) {
            throw exception(SALARY_CHANGE_TEMPLATE_DEFAULT_CANNOT_DELETE);
        }

        // 2. 删除调薪模板
        salaryChangeTemplateMapper.deleteById(id);
    }

    @Override
    public HrmSalaryChangeTemplateDO getSalaryChangeTemplate(Long id) {
        return salaryChangeTemplateMapper.selectById(id);
    }

    @Override
    public List<HrmSalaryChangeTemplateDO> getSalaryChangeTemplateList() {
        return salaryChangeTemplateMapper.selectListByIdDesc();
    }

    private void clearSalaryChangeTemplateDefault(Long excludeId) {
        for (HrmSalaryChangeTemplateDO template :
                salaryChangeTemplateMapper.selectListByDefaultStatus(true)) {
            if (ObjUtil.notEqual(template.getId(), excludeId)) {
                salaryChangeTemplateMapper.updateById(new HrmSalaryChangeTemplateDO()
                        .setId(template.getId()).setDefaultStatus(false));
            }
        }
    }

    private HrmSalaryChangeTemplateDO validateSalaryChangeTemplateExists(Long id) {
        HrmSalaryChangeTemplateDO template = salaryChangeTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(SALARY_CHANGE_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private List<Option> buildSalaryChangeOptions(List<HrmSalaryChangeOptionVO> options) {
        if (CollUtil.isEmpty(options)) {
            throw exception(SALARY_CHANGE_TEMPLATE_OPTION_INVALID);
        }
        // 1. 获得可用调薪项
        Map<Integer, HrmSalaryOptionDO> salaryOptionMap = convertMap(
                salaryOptionService.getSalaryOptionList(true), HrmSalaryOptionDO::getCode);
        // 2. 校验薪资项编码，并保存名称快照
        Set<Integer> optionCodes = new HashSet<>();
        return convertList(options, option -> {
            HrmSalaryOptionDO salaryOption = option == null ? null : salaryOptionMap.get(option.getCode());
            if (salaryOption == null || !optionCodes.add(option.getCode())) {
                throw exception(SALARY_CHANGE_TEMPLATE_OPTION_INVALID);
            }
            return Option.builder().name(salaryOption.getName()).code(salaryOption.getCode()).build();
        });
    }

}
