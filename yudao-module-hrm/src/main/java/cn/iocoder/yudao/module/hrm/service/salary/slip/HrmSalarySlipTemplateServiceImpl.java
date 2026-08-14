package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template.HrmSalarySlipTemplateOptionVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template.HrmSalarySlipTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip.HrmSalarySlipTemplateMapper;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionCodeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipTemplateCategoryEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipTemplateOptionTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getMaxValue;
import static cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO.ADJUSTABLE_CATEGORY_MAX_CODE;
import static cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO.ADJUSTABLE_CATEGORY_MIN_CODE;
import static cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO.ROOT_PARENT_CODE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_TEMPLATE_NOT_EXISTS;

/**
 * HRM 工资条模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalarySlipTemplateServiceImpl implements HrmSalarySlipTemplateService {

    @Resource
    private HrmSalarySlipTemplateMapper salarySlipTemplateMapper;
    @Resource
    private HrmSalaryOptionService salaryOptionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSalarySlipTemplate(HrmSalarySlipTemplateSaveReqVO createReqVO) {
        // 1. 构建工资条模板项
        List<HrmSalarySlipTemplateDO.Option> options =
                buildSalarySlipTemplateOptions(createReqVO.getOptions());

        // 2. 创建自定义工资条模板
        HrmSalarySlipTemplateDO template = new HrmSalarySlipTemplateDO()
                .setName(createReqVO.getName()).setHideEmpty(Boolean.TRUE.equals(createReqVO.getHideEmpty()))
                .setDefaultStatus(false).setOptions(options);
        salarySlipTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSalarySlipTemplate(HrmSalarySlipTemplateSaveReqVO updateReqVO) {
        // 1.1 校验工资条模板存在且不是默认模板
        HrmSalarySlipTemplateDO template = validateSalarySlipTemplateExists(updateReqVO.getId());
        if (Boolean.TRUE.equals(template.getDefaultStatus())) {
            throw exception(SALARY_DATA_ILLEGAL);
        }
        // 1.2 构建工资条模板项
        List<HrmSalarySlipTemplateDO.Option> options =
                buildSalarySlipTemplateOptions(updateReqVO.getOptions());

        // 2. 更新自定义工资条模板
        HrmSalarySlipTemplateDO updateObj = new HrmSalarySlipTemplateDO().setId(updateReqVO.getId())
                .setName(updateReqVO.getName()).setHideEmpty(Boolean.TRUE.equals(updateReqVO.getHideEmpty()))
                .setDefaultStatus(false).setOptions(options);
        salarySlipTemplateMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSalarySlipTemplate(Long id) {
        // 1. 校验工资条模板存在且不是默认模板
        HrmSalarySlipTemplateDO template = validateSalarySlipTemplateExists(id);
        if (Boolean.TRUE.equals(template.getDefaultStatus())) {
            throw exception(SALARY_DATA_ILLEGAL);
        }

        // 2. 删除工资条模板
        salarySlipTemplateMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HrmSalarySlipTemplateDO getSalarySlipTemplate(Long id) {
        HrmSalarySlipTemplateDO template = getSalarySlipTemplateByIdIncludingGlobalDefault(id);
        return buildDynamicSalarySlipTemplate(
                template, salaryOptionService.getSalaryOptionList(false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<HrmSalarySlipTemplateDO> getSalarySlipTemplateList() {
        List<HrmSalarySlipTemplateDO> templates = salarySlipTemplateMapper.selectListByDefaultStatusDesc();
        if (CollUtil.findOne(templates, template -> Boolean.TRUE.equals(template.getDefaultStatus())) == null) {
            HrmSalarySlipTemplateDO globalDefaultTemplate = getGlobalDefaultSalarySlipTemplate();
            if (globalDefaultTemplate != null) {
                templates.add(0, globalDefaultTemplate);
            }
        }
        List<HrmSalaryOptionDO> salaryOptions = salaryOptionService.getSalaryOptionList(false);
        return convertList(templates,
                template -> buildDynamicSalarySlipTemplate(template, salaryOptions));
    }

    @Override
    public HrmSalarySlipTemplateDO buildSalarySlipTemplateSnapshot(
            Boolean hideEmpty, List<HrmSalarySlipTemplateOptionVO> options) {
        return new HrmSalarySlipTemplateDO()
                .setHideEmpty(Boolean.TRUE.equals(hideEmpty))
                .setOptions(buildSalarySlipTemplateOptions(options));
    }

    // ==================== 动态补全 ====================

    /**
     * 动态补全工资条模板薪资项
     *
     * <p>系统默认模板始终展示当前有效的基本项和明细项；自定义模板保留用户布局，并把模板创建后新增的薪资项放入“未分类科目”。</p>
     *
     * @param template 工资条模板
     * @param salaryOptions 有效薪资项
     * @return 补全后的工资条模板
     */
    private HrmSalarySlipTemplateDO buildDynamicSalarySlipTemplate(
            HrmSalarySlipTemplateDO template, List<HrmSalaryOptionDO> salaryOptions) {
        // TODO DONE @AI：按空模板、默认模板、自定义模板三个分支补充方法内业务注释。
        // 1. 空模板无需补全
        if (template == null) {
            return null;
        }

        // 2. 默认模板始终按当前有效薪资项重建
        if (Boolean.TRUE.equals(template.getDefaultStatus())) {
            template.setOptions(buildDefaultSalarySlipTemplateOptions(salaryOptions));
            return template;
        }

        // 3. 自定义模板保留原布局，并将新增薪资项归入“未分类科目”
        List<HrmSalarySlipTemplateDO.Option> templateOptions =
                copySalarySlipTemplateOptions(template.getOptions());
        Set<Integer> selectedCodes = convertSetByFlatMap(templateOptions,
                option -> CollUtil.emptyIfNull(option.getChildren()).stream()
                        .map(HrmSalarySlipTemplateDO.Option::getCode));
        selectedCodes.addAll(convertSet(templateOptions, HrmSalarySlipTemplateDO.Option::getCode,
                option -> ObjectUtil.equal(option.getType(), HrmSalarySlipTemplateOptionTypeEnum.ITEM.getType())));
        List<HrmSalaryOptionDO> detailOptions = getSalarySlipDetailOptions(salaryOptions);
        List<HrmSalaryOptionDO> missingOptions = filterList(detailOptions,
                option -> !selectedCodes.contains(option.getCode()));
        if (CollUtil.isNotEmpty(missingOptions)) {
            Set<Integer> usedCodes = convertSet(templateOptions, HrmSalarySlipTemplateDO.Option::getCode);
            int categoryCode = HrmSalarySlipTemplateCategoryEnum.BASIC.getCode();
            for (int i = 0; i <= usedCodes.size(); i++) {
                if (!usedCodes.contains(categoryCode)) {
                    break;
                }
                categoryCode--;
            }
            Integer maxSort = getMaxValue(
                    filterList(templateOptions, option -> option.getSort() != null),
                    HrmSalarySlipTemplateDO.Option::getSort);
            int categorySort = (maxSort == null ? 0 : maxSort) + 1;
            templateOptions.add(HrmSalarySlipTemplateDO.Option.builder()
                    .name("未分类科目").type(HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType()).code(categoryCode)
                    .hidden(false).sort(categorySort)
                    .children(buildSalarySlipTemplateItems(missingOptions)).build());
        }
        template.setOptions(templateOptions);
        return template;
    }

    /**
     * 构建系统默认工资条模板项
     *
     * @param salaryOptions 有效薪资项
     * @return 默认工资条模板项
     */
    private List<HrmSalarySlipTemplateDO.Option> buildDefaultSalarySlipTemplateOptions(
            List<HrmSalaryOptionDO> salaryOptions) {
        // 1. 构建基本项分类
        List<HrmSalaryOptionDO> basicOptions = filterList(salaryOptions,
                option -> HrmSalaryOptionCodeEnum.SALARY_SLIP_BASIC_CODES.contains(option.getCode()));
        basicOptions.sort(Comparator.comparing(HrmSalaryOptionDO::getCode));
        HrmSalarySlipTemplateDO.Option basicCategory = HrmSalarySlipTemplateDO.Option.builder()
                .name(HrmSalarySlipTemplateCategoryEnum.BASIC.getName()).type(HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType()).code(HrmSalarySlipTemplateCategoryEnum.BASIC.getCode())
                .hidden(false).sort(HrmSalarySlipTemplateCategoryEnum.BASIC.getSort()).children(buildSalarySlipTemplateItems(basicOptions)).build();

        // 2. 构建明细项分类
        HrmSalarySlipTemplateDO.Option detailCategory = HrmSalarySlipTemplateDO.Option.builder()
                .name(HrmSalarySlipTemplateCategoryEnum.DETAIL.getName()).type(HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType()).code(HrmSalarySlipTemplateCategoryEnum.DETAIL.getCode())
                .hidden(false).sort(HrmSalarySlipTemplateCategoryEnum.DETAIL.getSort())
                .children(buildSalarySlipTemplateItems(getSalarySlipDetailOptions(salaryOptions))).build();
        return Arrays.asList(basicCategory, detailCategory);
    }

    /**
     * 获得工资条明细薪资项
     *
     * @param salaryOptions 有效薪资项
     * @return 明细薪资项
     */
    private List<HrmSalaryOptionDO> getSalarySlipDetailOptions(List<HrmSalaryOptionDO> salaryOptions) {
        List<HrmSalaryOptionDO> detailOptions = filterList(salaryOptions,
                option -> option.getParentCode() != null
                        && option.getParentCode() >= ADJUSTABLE_CATEGORY_MIN_CODE
                        && option.getParentCode() <= ADJUSTABLE_CATEGORY_MAX_CODE);
        detailOptions.sort(Comparator.comparing(HrmSalaryOptionDO::getCode));
        return detailOptions;
    }

    /**
     * 转换工资条模板明细项
     *
     * @param salaryOptions 薪资项
     * @return 工资条模板明细项
     */
    private List<HrmSalarySlipTemplateDO.Option> buildSalarySlipTemplateItems(
            List<HrmSalaryOptionDO> salaryOptions) {
        List<HrmSalarySlipTemplateDO.Option> options = new ArrayList<>();
        for (int i = 0; i < salaryOptions.size(); i++) {
            HrmSalaryOptionDO salaryOption = salaryOptions.get(i);
            options.add(HrmSalarySlipTemplateDO.Option.builder()
                    .name(salaryOption.getName()).type(HrmSalarySlipTemplateOptionTypeEnum.ITEM.getType()).code(salaryOption.getCode())
                    .remark(salaryOption.getRemark()).hidden(false).sort(i + 1)
                    .children(Collections.emptyList()).build());
        }
        return options;
    }

    /**
     * 复制工资条模板项，避免动态补全修改持久化对象
     *
     * @param options 工资条模板项
     * @return 复制后的工资条模板项
     */
    private List<HrmSalarySlipTemplateDO.Option> copySalarySlipTemplateOptions(
            List<HrmSalarySlipTemplateDO.Option> options) {
        return convertList(options, option -> BeanUtils.toBean(option, HrmSalarySlipTemplateDO.Option.class,
                copy -> copy.setChildren(copySalarySlipTemplateOptions(option.getChildren()))));
    }

    // ==================== 校验 ====================

    private HrmSalarySlipTemplateDO validateSalarySlipTemplateExists(Long id) {
        HrmSalarySlipTemplateDO template = getSalarySlipTemplateByIdIncludingGlobalDefault(id);
        if (template == null) {
            throw exception(SALARY_SLIP_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 获得租户工资条模板，必要时回退平台级默认模板
     *
     * @param id 工资条模板编号
     * @return 工资条模板
     */
    private HrmSalarySlipTemplateDO getSalarySlipTemplateByIdIncludingGlobalDefault(Long id) {
        HrmSalarySlipTemplateDO template = salarySlipTemplateMapper.selectById(id);
        if (template != null) {
            return template;
        }
        HrmSalarySlipTemplateDO globalDefaultTemplate = getGlobalDefaultSalarySlipTemplate();
        return globalDefaultTemplate != null && ObjectUtil.equal(globalDefaultTemplate.getId(), id)
                ? globalDefaultTemplate : null;
    }

    /**
     * 获得平台级默认工资条模板
     *
     * @return 平台级默认工资条模板
     */
    private HrmSalarySlipTemplateDO getGlobalDefaultSalarySlipTemplate() {
        return TenantUtils.executeIgnore(salarySlipTemplateMapper::selectGlobalDefaultTemplate);
    }

    // ==================== 保存模板项 ====================

    /**
     * 将前端提交的扁平模板节点重建为持久化的两层结构。
     *
     * <p>方法会校验节点类型、编码唯一性和父分类引用，并在缺少时补全不可缺少的“实发工资”项，
     * 最后按排序值输出稳定结果。</p>
     *
     * @param optionVOs 前端提交的扁平模板节点
     * @return 持久化的工资条模板项
     */
    private List<HrmSalarySlipTemplateDO.Option> buildSalarySlipTemplateOptions(
            List<HrmSalarySlipTemplateOptionVO> optionVOs) {
        List<HrmSalarySlipTemplateOptionVO> sortedOptions = convertList(optionVOs, option -> option);
        sortedOptions.sort(Comparator.comparing(option -> option.getSort() == null
                ? Integer.MAX_VALUE : option.getSort()));

        // 1. 校验节点并构建分类
        List<HrmSalarySlipTemplateDO.Option> options = new ArrayList<>();
        Map<Integer, HrmSalarySlipTemplateDO.Option> categoryMap = new HashMap<>();
        Set<Integer> optionCodes = new HashSet<>();
        for (HrmSalarySlipTemplateOptionVO option : sortedOptions) {
            if ((ObjectUtil.notEqual(option.getType(), HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType())
                    && ObjectUtil.notEqual(option.getType(), HrmSalarySlipTemplateOptionTypeEnum.ITEM.getType()))
                    || option.getCode() == null || !optionCodes.add(option.getCode())) {
                throw exception(SALARY_DATA_ILLEGAL);
            }
            if (ObjectUtil.notEqual(option.getType(), HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType())) {
                continue;
            }
            if (option.getParentCode() != null && ObjectUtil.notEqual(option.getParentCode(), ROOT_PARENT_CODE)) {
                throw exception(SALARY_DATA_ILLEGAL);
            }
            HrmSalarySlipTemplateDO.Option category = buildSalarySlipTemplateOption(option);
            categoryMap.put(category.getCode(), category);
            options.add(category);
        }

        // 2. 将薪资项放入根节点或对应分类
        for (HrmSalarySlipTemplateOptionVO option : sortedOptions) {
            if (ObjectUtil.equal(option.getType(), HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType())) {
                continue;
            }
            HrmSalarySlipTemplateDO.Option item = buildSalarySlipTemplateOption(option);
            HrmSalarySlipTemplateDO.Option category = categoryMap.get(option.getParentCode());
            if (option.getParentCode() == null || ObjectUtil.equal(option.getParentCode(), ROOT_PARENT_CODE)) {
                options.add(item);
            } else if (category == null) {
                throw exception(SALARY_DATA_ILLEGAL);
            } else {
                category.getChildren().add(item);
            }
        }
        if (!optionCodes.contains(HrmSalaryOptionCodeEnum.REAL_PAY.getCode())) {
            options.add(HrmSalarySlipTemplateDO.Option.builder()
                    .name(HrmSalaryOptionCodeEnum.REAL_PAY.getName()).type(HrmSalarySlipTemplateOptionTypeEnum.ITEM.getType()).code(HrmSalaryOptionCodeEnum.REAL_PAY.getCode())
                    .hidden(false).sort(HrmSalaryOptionCodeEnum.REAL_PAY.getCode()).children(Collections.emptyList()).build());
        }
        options.sort(Comparator.comparing(option -> option.getSort() == null
                ? Integer.MAX_VALUE : option.getSort()));
        return options;
    }

    private HrmSalarySlipTemplateDO.Option buildSalarySlipTemplateOption(
            HrmSalarySlipTemplateOptionVO option) {
        return HrmSalarySlipTemplateDO.Option.builder()
                .name(option.getName()).type(option.getType()).code(option.getCode()).remark(option.getRemark())
                .hidden(ObjectUtil.notEqual(option.getCode(), HrmSalaryOptionCodeEnum.REAL_PAY.getCode())
                        && Boolean.TRUE.equals(option.getHidden()))
                .sort(option.getSort() == null ? option.getCode() : option.getSort())
                .children(new ArrayList<>()).build();
    }

}
