package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule.MesMdAutoCodeRulePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule.MesMdAutoCodeRuleSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodePartMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodeRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 编码规则 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdAutoCodeRuleServiceImpl implements MesMdAutoCodeRuleService {

    @Resource
    private MesMdAutoCodeRuleMapper ruleMapper;

    @Resource
    private MesMdAutoCodePartMapper partMapper;

    @Override
    public Long createAutoCodeRule(MesMdAutoCodeRuleSaveReqVO createReqVO) {
        // 校验规则编码的唯一性
        validateRuleCodeUnique(null, createReqVO.getCode());

        // 插入
        MesMdAutoCodeRuleDO rule = BeanUtils.toBean(createReqVO, MesMdAutoCodeRuleDO.class);
        ruleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public void updateAutoCodeRule(MesMdAutoCodeRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateAutoCodeRuleExists(updateReqVO.getId());
        // 校验规则编码的唯一性
        validateRuleCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesMdAutoCodeRuleDO updateObj = BeanUtils.toBean(updateReqVO, MesMdAutoCodeRuleDO.class);
        ruleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoCodeRule(Long id) {
        // 校验存在
        validateAutoCodeRuleExists(id);

        // 删除规则
        ruleMapper.deleteById(id);
        // 级联删除规则组成
        partMapper.deleteByRuleId(id);
    }

    @Override
    public MesMdAutoCodeRuleDO getAutoCodeRule(Long id) {
        return ruleMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdAutoCodeRuleDO> getAutoCodeRulePage(MesMdAutoCodeRulePageReqVO pageReqVO) {
        return ruleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdAutoCodeRuleDO> getAutoCodeRuleListByStatus(Integer status) {
        return ruleMapper.selectListByStatus(status);
    }

    @Override
    public MesMdAutoCodeRuleDO getAutoCodeRuleByCode(String code) {
        return ruleMapper.selectByCode(code);
    }

    @Override
    public void validateAutoCodeRuleExists(Long id) {
        if (ruleMapper.selectById(id) == null) {
            throw exception(AUTO_CODE_RULE_NOT_EXISTS);
        }
    }

    // ==================== 校验方法 ====================

    private void validateRuleCodeUnique(Long id, String code) {
        MesMdAutoCodeRuleDO rule = ruleMapper.selectByCode(code);
        if (rule == null) {
            return;
        }
        if (ObjUtil.notEqual(id, rule.getId())) {
            throw exception(AUTO_CODE_RULE_CODE_DUPLICATE);
        }
    }

}
