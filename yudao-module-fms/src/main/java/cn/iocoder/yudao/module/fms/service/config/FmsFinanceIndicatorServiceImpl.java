package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeindicator.FmsFinanceIndicatorSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceIndicatorDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsFinanceIndicatorMapper;
import cn.iocoder.yudao.module.fms.service.report.FmsReportCommonService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.HOME_METRIC_INVALID;

/**
 * FMS 首页财务指标 Service 实现类
 *
 * 指标公式复用报表已有表达式：行次公式使用 L1+L2，科目公式使用报表公式 JSON。
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsFinanceIndicatorServiceImpl implements FmsFinanceIndicatorService {

    // TODO DONE @AI：补充行次公式正则说明。
    /**
     * 行次公式格式，例如 {@code L1+L2-L3}
     */
    private static final Pattern LINE_FORMULA_PATTERN = Pattern.compile("[+-]?L\\d+(?:[+-]L\\d+)*");

    private static final String PRESET_RESOURCE = "fms/config/finance-indicator-presets.json";

    @Resource
    private FmsFinanceIndicatorMapper financeIndicatorMapper;

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsSubjectService subjectService;
    @Resource
    private FmsReportCommonService reportCommonService;

    @Override
    public Long createFinanceIndicator(FmsFinanceIndicatorSaveReqVO reqVO, Long userId) {
        // TODO DONE @AI：方法步骤已对齐 FMS Service 风格。
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(reqVO.getAccountSetId(), userId);
        // 1.2 校验指标公式
        validateSaveReq(reqVO);
        // 1.3 校验指标编码唯一
        validateCodeUnique(reqVO.getAccountSetId(), null, reqVO.getCode());

        // 2. 创建财务指标
        FmsFinanceIndicatorDO indicator = BeanUtils.toBean(reqVO, FmsFinanceIndicatorDO.class).setId(null)
                .setFormula(bindFormula(reqVO.getAccountSetId(), normalizeFormula(reqVO.getFormula()), userId));
        financeIndicatorMapper.insert(indicator);
        return indicator.getId();
    }

    @Override
    public void updateFinanceIndicator(FmsFinanceIndicatorSaveReqVO reqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(reqVO.getAccountSetId(), userId);
        // 1.2 校验财务指标存在
        validateFinanceIndicatorExists(reqVO.getAccountSetId(), reqVO.getId());
        // 1.3 校验指标公式
        validateSaveReq(reqVO);
        // 1.4 校验指标编码唯一
        validateCodeUnique(reqVO.getAccountSetId(), reqVO.getId(), reqVO.getCode());

        // 2. 更新指标
        financeIndicatorMapper.updateById(BeanUtils.toBean(reqVO, FmsFinanceIndicatorDO.class)
                .setFormula(bindFormula(reqVO.getAccountSetId(), normalizeFormula(reqVO.getFormula()), userId)));
    }

    @Override
    public void deleteFinanceIndicator(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验财务指标存在
        validateFinanceIndicatorExists(accountSetId, id);

        // 2. 删除指标
        financeIndicatorMapper.deleteById(id);
    }

    @Override
    public List<FmsFinanceIndicatorDO> getFinanceIndicatorList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 2. 查询首页财务指标
        return financeIndicatorMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public FmsFinanceIndicatorDO getFinanceIndicator(Long accountSetId, Long id, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询并校验财务指标
        return validateFinanceIndicatorExists(accountSetId, id);
    }

    @Override
    public List<FmsFinanceIndicatorDO> getEnabledFinanceIndicatorList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        // 2. 查询已启用的首页财务指标
        return financeIndicatorMapper.selectListByAccountSetIdAndStatus(accountSetId,
                CommonStatusEnum.ENABLE.getStatus());
    }

    private FmsFinanceIndicatorDO validateFinanceIndicatorExists(Long accountSetId, Long id) {
        FmsFinanceIndicatorDO indicator = financeIndicatorMapper.selectByIdAndAccountSetId(id, accountSetId);
        if (indicator == null) {
            throw exception(HOME_METRIC_INVALID);
        }
        return indicator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeDefaultFinanceIndicators(Long accountSetId, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验财务指标尚未初始化
        if (CollUtil.isNotEmpty(financeIndicatorMapper.selectListByAccountSetId(accountSetId))) {
            return;
        }

        // 2. 加载预置配置并绑定当前账套科目
        List<Preset> presets = loadPresetList();
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode);
        financeIndicatorMapper.insertBatch(convertList(presets, preset -> new FmsFinanceIndicatorDO()
                .setAccountSetId(accountSetId).setCode(preset.getCode()).setName(preset.getName())
                .setType(preset.getType()).setFormula(bindFormula(normalizeFormula(preset.getFormula()), subjectMap))
                .setSort(preset.getSort()).setStatus(CommonStatusEnum.ENABLE.getStatus())));
    }

    private void validateSaveReq(FmsFinanceIndicatorSaveReqVO reqVO) {
        if (!isValidFormula(reqVO.getFormula())) {
            throw exception(HOME_METRIC_INVALID);
        }
    }

    private boolean isValidFormula(String formula) {
        if (StrUtil.isBlank(formula)) {
            return false;
        }
        if (LINE_FORMULA_PATTERN.matcher(formula.replace(" ", "")).matches()) {
            return true;
        }
        try {
            return JsonUtils.parseArray(formula, Object.class) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String normalizeFormula(String formula) {
        String normalized = formula.replace(" ", "");
        return LINE_FORMULA_PATTERN.matcher(normalized).matches()
                ? JsonUtils.toJsonString(Collections.singletonList(normalized)) : formula;
    }

    private String bindFormula(Long accountSetId, String formula, Long userId) {
        List<FmsSubjectDO> subjects = subjectService.getSubjectList(accountSetId, null, userId);
        Map<String, FmsSubjectDO> subjectMap = convertMap(subjects, FmsSubjectDO::getCode);
        return bindFormula(formula, subjectMap);
    }

    private String bindFormula(String formula, Map<String, FmsSubjectDO> subjectMap) {
        if (StrUtil.isBlank(formula) || reportCommonService.isLineFormula(formula)) {
            return formula;
        }
        return reportCommonService.bindSubjectFormula(formula, subjectMap);
    }

    /**
     * 加载财务指标预置文件
     *
     * @return 财务指标预置配置数组
     */
    private List<Preset> loadPresetList() {
        try (InputStream inputStream = new ClassPathResource(PRESET_RESOURCE).getInputStream()) {
            return JsonUtils.parseArray(IoUtil.readUtf8(inputStream), Preset.class);
        } catch (Exception exception) {
            throw new IllegalStateException("首页财务指标预置文件加载失败", exception);
        }
    }

    private void validateCodeUnique(Long accountSetId, Long id, String code) {
        FmsFinanceIndicatorDO indicator = financeIndicatorMapper.selectByAccountSetIdAndCode(accountSetId, code);
        if (indicator != null && ObjUtil.notEqual(indicator.getId(), id)) {
            throw exception(HOME_METRIC_INVALID);
        }
    }

    @Data
    private static class Preset {

        /**
         * 指标编码
         */
        private String code;
        /**
         * 指标名称
         */
        private String name;
        /**
         * 取数报表类型
         */
        private Integer type;
        /**
         * 指标公式
         */
        private String formula;
        /**
         * 展示顺序
         */
        private Integer sort;
    }
}
