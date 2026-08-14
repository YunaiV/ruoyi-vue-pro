package cn.iocoder.yudao.module.fms.service.closing;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingTemplateRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingTemplateSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingTemplateMapper;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTemplateCategoryEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.sumBigDecimal;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_SCHEME_RATIO_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_SCHEME_SUBJECT_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_TEMPLATE_PRESET_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_TEMPLATE_PRESET_SUBJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CLOSING_TEMPLATE_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CLOSING_TEMPLATE_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CLOSING_TEMPLATE_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CLOSING_TEMPLATE_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CLOSING_TEMPLATE_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CLOSING_TEMPLATE_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_CLOSING_TYPE;

/**
 * FMS 结账模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsClosingTemplateServiceImpl implements FmsClosingTemplateService {

    private static final String PRESET_RESOURCE = "fms/closing/closing-template-presets.json";

    @Resource
    private FmsClosingTemplateMapper closingTemplateMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsFinanceParameterService financeParameterService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeClosingTemplates(Long accountSetId, Long userId) {
        // 1.1 加载并校验预置模板
        List<Preset> presets = loadPresets();
        validatePresets(presets);
        // 1.2 查询尚未初始化的预置编码
        Set<String> existingCodes = convertSet(closingTemplateMapper.selectListByAccountSetId(accountSetId),
                FmsClosingTemplateDO::getPresetCode, Objects::nonNull);
        List<Preset> pendingPresets = filterList(presets,
                preset -> !existingCodes.contains(preset.getCode()));
        if (CollUtil.isEmpty(pendingPresets)) {
            return;
        }

        // 2. 查询账套科目并建立实际科目编码索引
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode,
                Function.identity(), (first, second) -> first);
        FmsFinanceParameterDO financeParameter = financeParameterService.getFinanceParameter(accountSetId);

        // 3. 转换预置模板并批量创建
        List<FmsClosingTemplateDO> templates = convertList(pendingPresets,
                preset -> buildPresetTemplate(accountSetId, preset, subjectMap,
                        financeParameter.getSubjectCodeRule()));
        closingTemplateMapper.insertBatch(templates);
    }

    @Override
    public List<FmsClosingTemplateRespVO> getClosingTemplateList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询结账模板
        return convertList(closingTemplateMapper.selectListByAccountSetId(accountSetId), this::buildTemplateRespVO);
    }

    @Override
    public Long getClosingTemplateCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        // 1. 查询账套结账模板
        List<FmsClosingTemplateDO> templates = closingTemplateMapper.selectListByAccountSetId(accountSetId);

        // 2. 统计来源科目或分录科目被引用的模板数量
        Set<Long> subjectIdSet = new HashSet<>(subjectIds);
        return (long) filterList(templates, template -> subjectIdSet.contains(template.getSubjectId())
                || CollUtil.emptyIfNull(template.getSubjectRules()).stream()
                .anyMatch(rule -> subjectIdSet.contains(rule.getSubjectId()))).size();
    }

    @Override
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_TEMPLATE_CREATE_SUB_TYPE,
            bizNo = "{{#templateId}}", success = FMS_CLOSING_TEMPLATE_CREATE_SUCCESS)
    public Long createClosingTemplate(FmsClosingTemplateSaveReqVO createReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);
        // 1.2 校验模板科目和金额比例
        Map<Long, FmsSubjectDO> subjectMap = validateClosingTemplate(createReqVO, userId);

        // 2. 创建结账模板
        FmsClosingTemplateDO template = buildClosingTemplate(createReqVO, subjectMap).setId(null);
        closingTemplateMapper.insert(template);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("templateId", template.getId());
        return template.getId();
    }

    @Override
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_TEMPLATE_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_CLOSING_TEMPLATE_UPDATE_SUCCESS)
    public void updateClosingTemplate(FmsClosingTemplateSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验结账模板存在
        validateClosingTemplateExists(updateReqVO.getAccountSetId(), updateReqVO.getId());
        // 1.3 校验模板科目和金额比例
        Map<Long, FmsSubjectDO> subjectMap = validateClosingTemplate(updateReqVO, userId);

        // 2. 更新结账模板
        closingTemplateMapper.updateById(buildClosingTemplate(updateReqVO, subjectMap));
    }

    @Override
    @LogRecord(type = FMS_CLOSING_TYPE, subType = FMS_CLOSING_TEMPLATE_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_CLOSING_TEMPLATE_DELETE_SUCCESS)
    public void deleteClosingTemplate(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验结账模板存在
        validateClosingTemplateExists(accountSetId, id);

        // 2. 删除结账模板
        closingTemplateMapper.deleteById(id);
    }

    /**
     * 校验结账模板存在
     *
     * @param accountSetId 账套编号
     * @param id 模板编号
     * @return 结账模板
     */
    private FmsClosingTemplateDO validateClosingTemplateExists(Long accountSetId, Long id) {
        FmsClosingTemplateDO template = closingTemplateMapper.selectByIdAndAccountSetId(id, accountSetId);
        if (template == null) {
            throw exception(CLOSING_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 校验模板科目和金额比例
     *
     * @param saveReqVO 模板信息
     * @param userId 用户编号
     * @return 科目编号到科目的 Map
     */
    private Map<Long, FmsSubjectDO> validateClosingTemplate(
            FmsClosingTemplateSaveReqVO saveReqVO, Long userId) {
        // 1. 校验来源科目和规则科目存在且为末级科目
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(saveReqVO.getAccountSetId(), null, userId);
        Map<Long, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getId);
        Set<Long> parentIds = convertSet(subjects, FmsSubjectDO::getParentId, Objects::nonNull);
        if (saveReqVO.getSubjectId() != null
                && (!subjectMap.containsKey(saveReqVO.getSubjectId()) || parentIds.contains(saveReqVO.getSubjectId()))) {
            throw exception(CLOSING_SCHEME_SUBJECT_INVALID);
        }
        for (FmsClosingTemplateSaveReqVO.SubjectRule rule : saveReqVO.getSubjects()) {
            if (!subjectMap.containsKey(rule.getSubjectId()) || parentIds.contains(rule.getSubjectId())) {
                throw exception(CLOSING_SCHEME_SUBJECT_INVALID);
            }
        }

        // 2. 校验借贷金额比例分别等于 100%
        BigDecimal debitRatio = sumBigDecimal(filterList(saveReqVO.getSubjects(), rule -> Objects.equals(
                rule.getDirection(), FmsDebitCreditDirectionEnum.DEBIT.getType())),
                FmsClosingTemplateSaveReqVO.SubjectRule::getAmountRatio);
        BigDecimal creditRatio = sumBigDecimal(filterList(saveReqVO.getSubjects(), rule -> Objects.equals(
                rule.getDirection(), FmsDebitCreditDirectionEnum.CREDIT.getType())),
                FmsClosingTemplateSaveReqVO.SubjectRule::getAmountRatio);
        if (debitRatio.compareTo(BigDecimal.valueOf(100)) != 0
                || creditRatio.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw exception(CLOSING_SCHEME_RATIO_INVALID);
        }
        return subjectMap;
    }

    /**
     * 构造结账模板
     *
     * @param saveReqVO 模板信息
     * @param subjectMap 科目编号到科目的 Map
     * @return 结账模板
     */
    private FmsClosingTemplateDO buildClosingTemplate(FmsClosingTemplateSaveReqVO saveReqVO,
            Map<Long, FmsSubjectDO> subjectMap) {
        List<FmsClosingTemplateDO.SubjectRule> rules = BeanUtils.toBean(
                saveReqVO.getSubjects(), FmsClosingTemplateDO.SubjectRule.class);
        rules.forEach(rule -> rule.setSubjectCode(subjectMap.get(rule.getSubjectId()).getCode()));
        return BeanUtils.toBean(saveReqVO, FmsClosingTemplateDO.class).setSubjectRules(rules);
    }

    /**
     * 构造结账模板响应
     *
     * @param template 结账模板
     * @return 结账模板响应
     */
    private FmsClosingTemplateRespVO buildTemplateRespVO(FmsClosingTemplateDO template) {
        return BeanUtils.toBean(template, FmsClosingTemplateRespVO.class)
                .setSubjects(BeanUtils.toBean(template.getSubjectRules(), FmsClosingTemplateRespVO.SubjectRule.class));
    }

    /**
     * 构造预置结账模板
     *
     * @param accountSetId 账套编号
     * @param preset 预置模板
     * @param subjectMap 实际科目编码到科目的 Map
     * @param subjectCodeRule 科目编码规则
     * @return 结账模板
     */
    private FmsClosingTemplateDO buildPresetTemplate(Long accountSetId, Preset preset,
            Map<String, FmsSubjectDO> subjectMap, String subjectCodeRule) {
        List<FmsClosingTemplateDO.SubjectRule> rules = convertList(preset.getEntries(), entry -> {
            String actualCode = financeParameterService.convertStandardSubjectCode(
                    entry.getSubjectCode(), subjectCodeRule);
            FmsSubjectDO subject = subjectMap.get(actualCode);
            if (subject == null) {
                throw exception(CLOSING_TEMPLATE_PRESET_SUBJECT_NOT_EXISTS, preset.getCode(), entry.getSubjectCode());
            }
            return FmsClosingTemplateDO.SubjectRule.builder().subjectId(subject.getId())
                    .subjectCode(subject.getCode()).digest(entry.getDigest())
                    .direction(FmsDebitCreditDirectionEnum.valueOf(entry.getDirection()).getType())
                    .amountRatio(entry.getAmountRatio()).build();
        });
        return new FmsClosingTemplateDO().setAccountSetId(accountSetId).setPresetCode(preset.getCode())
                .setName(preset.getName()).setCategory(FmsClosingTemplateCategoryEnum.valueOf(preset.getCategory()).getCategory())
                .setPeriodEnd(true).setSubjectRules(rules).setSort(preset.getSort());
    }

    /**
     * 加载结账模板预置文件
     *
     * @return 预置文件
     */
    private List<Preset> loadPresets() {
        try (InputStream inputStream = new ClassPathResource(PRESET_RESOURCE).getInputStream()) {
            return JsonUtils.parseArray(IoUtil.readUtf8(inputStream), Preset.class);
        } catch (Exception ex) {
            throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
        }
    }

    /**
     * 校验结账模板预置文件
     *
     * @param presets 预置模板数组
     */
    private void validatePresets(List<Preset> presets) {
        // 1. 校验预置编码唯一
        if (CollUtil.isEmpty(presets) || convertSet(presets, Preset::getCode).size() != presets.size()) {
            throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
        }
        for (Preset preset : presets) {
            // 2. 校验模板分类、分录数量和枚举字段
            try {
                FmsClosingTemplateCategoryEnum.valueOf(preset.getCategory());
                if (CollUtil.size(preset.getEntries()) < 2) {
                    throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
                }
                for (Preset.Entry entry : preset.getEntries()) {
                    FmsDebitCreditDirectionEnum.valueOf(entry.getDirection());
                }
            } catch (IllegalArgumentException | NullPointerException ex) {
                throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
            }
            // 3. 校验借贷金额比例分别等于 100%
            BigDecimal debitRatio = sumBigDecimal(filterList(preset.getEntries(), entry -> Objects.equals(
                    entry.getDirection(), FmsDebitCreditDirectionEnum.DEBIT.name())), Preset.Entry::getAmountRatio);
            BigDecimal creditRatio = sumBigDecimal(filterList(preset.getEntries(), entry -> Objects.equals(
                    entry.getDirection(), FmsDebitCreditDirectionEnum.CREDIT.name())), Preset.Entry::getAmountRatio);
            if (debitRatio.compareTo(BigDecimal.valueOf(100)) != 0
                    || creditRatio.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw exception(CLOSING_TEMPLATE_PRESET_INVALID);
            }
        }
    }

    @Data
    private static class Preset {

        /**
         * 预置编码
         */
        private String code;
        /**
         * 模板名称
         */
        private String name;
        /**
         * 模板分类
         */
        private String category;
        /**
         * 显示顺序
         */
        private Integer sort;
        /**
         * 分录数组
         */
        private List<Entry> entries;

        @Data
        private static class Entry {

            /**
             * 标准科目编码
             */
            private String subjectCode;
            /**
             * 摘要
             */
            private String digest;
            /**
             * 借贷方向
             */
            private String direction;
            /**
             * 金额比例
             */
            private BigDecimal amountRatio;
        }
    }

}
