package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodeRecordMapper;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePaddedMethodEnum;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodeContext;
import cn.iocoder.yudao.module.mes.service.md.autocode.strategy.MesMdAutoCodePartStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 编码生成记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdAutoCodeRecordServiceImpl implements MesMdAutoCodeRecordService {

    @Resource
    private MesMdAutoCodeRecordMapper recordMapper;

    @Resource
    private MesMdAutoCodeRuleService ruleService;
    @Resource
    private MesMdAutoCodePartService partService;

    @Resource
    private List<MesMdAutoCodePartStrategy> strategyList;
    private Map<Integer, MesMdAutoCodePartStrategy> strategyMap;

    @PostConstruct
    public void init() {
        strategyMap = CollectionUtils.convertMap(strategyList, MesMdAutoCodePartStrategy::getType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateAutoCode(String ruleCode, String inputChar) {
        // 1.1 查询规则
        MesMdAutoCodeRuleDO rule = ruleService.getAutoCodeRuleByCode(ruleCode);
        if (rule == null) {
            throw exception(AUTO_CODE_RULE_NOT_EXISTS);
        }
        // 1.2 查询规则组成
        List<MesMdAutoCodePartDO> parts = partService.getAutoCodePartListByRuleId(rule.getId());
        if (CollUtil.isEmpty(parts)) {
            throw exception(AUTO_CODE_GENERATE_FAILED);
        }

        // 2.1 构建上下文
        MesMdAutoCodeContext context = new MesMdAutoCodeContext()
                .setRule(rule).setParts(parts).setInputChar(inputChar);
        // 2.2 遍历分段，生成编码
        StringBuilder codeBuilder = new StringBuilder();
        for (MesMdAutoCodePartDO part : parts) {
            MesMdAutoCodePartStrategy strategy = strategyMap.get(part.getType());
            Assert.notNull(strategy, () -> exception(AUTO_CODE_GENERATE_FAILED));
            // 生成分段编码
            String partCode = strategy.generate(part, context);
            // 统一截取指定长度
            partCode = StrUtil.sub(partCode, 0, part.getLength());
            // 拼接分段编码
            codeBuilder.append(partCode);
        }
        // 2.3 补齐处理
        String result = codeBuilder.toString();
        if (Boolean.TRUE.equals(rule.getPadded()) && rule.getMaxLength() != null) {
            result = padCode(result, rule);
        }

        // 3.1 二次校验（防止重复）
        MesMdAutoCodeRecordDO existRecord = recordMapper.selectByResult(result);
        if (existRecord != null) {
            throw exception(AUTO_CODE_GENERATE_FAILED);
        }
        // 3.2 保存生成记录
        MesMdAutoCodeRecordDO record = new MesMdAutoCodeRecordDO()
                .setRuleId(rule.getId()).setResult(result)
                .setSerialNo(context.getSerialNo()).setInputChar(inputChar);
        recordMapper.insert(record);
        return result;
    }

    /**
     * 补齐编码
     */
    private String padCode(String code, MesMdAutoCodeRuleDO rule) {
        if (code.length() >= rule.getMaxLength()) {
            return StrUtil.sub(code, 0, rule.getMaxLength());
        }
        String paddedChar = StrUtil.emptyToDefault(rule.getPaddedChar(), "0");
        Integer paddedMethod = rule.getPaddedMethod() != null ? rule.getPaddedMethod() : MesMdAutoCodePaddedMethodEnum.LEFT.getMethod();
        if (MesMdAutoCodePaddedMethodEnum.LEFT.getMethod().equals(paddedMethod)) {
            // 左补齐
            return StrUtil.padPre(code, rule.getMaxLength(), paddedChar.charAt(0));
        } else {
            // 右补齐
            return StrUtil.padAfter(code, rule.getMaxLength(), paddedChar.charAt(0));
        }
    }

}
