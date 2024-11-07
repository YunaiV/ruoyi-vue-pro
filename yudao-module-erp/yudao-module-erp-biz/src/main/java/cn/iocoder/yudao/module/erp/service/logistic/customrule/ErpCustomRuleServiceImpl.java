package cn.iocoder.yudao.module.erp.service.logistic.customrule;

import cn.iocoder.yudao.module.erp.aop.SynExternalData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.aop.SynExternalDataAspect.ERP_CUSTOM_RULE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 海关规则 Service 实现类
 *
 * @author 索迈管理员
 */
@Slf4j
@Service
@Validated
public class ErpCustomRuleServiceImpl implements ErpCustomRuleService {

    @Resource
    private ErpCustomRuleMapper customRuleMapper;

    @Override
    @SynExternalData(table = ERP_CUSTOM_RULE)
    public Long createCustomRule(ErpCustomRuleSaveReqVO createReqVO) {
        log.info("create custom rule");
        // 插入
        ErpCustomRuleDO customRule = BeanUtils.toBean(createReqVO, ErpCustomRuleDO.class);
        log.info(customRuleMapper.selectList().toString());
        customRuleMapper.insert(customRule);
        log.info(customRuleMapper.selectList().toString());
        // 返回
        return customRule.getId();
    }

    @Override
    @SynExternalData(table = ERP_CUSTOM_RULE, inClazz = ErpCustomRuleSaveReqVO.class)
    public void updateCustomRule(ErpCustomRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateCustomRuleExists(updateReqVO.getId());
        // 更新
        ErpCustomRuleDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomRuleDO.class);
        customRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteCustomRule(Long id) {
        // 校验存在
        validateCustomRuleExists(id);
        // 删除
        customRuleMapper.deleteById(id);
    }

    private void validateCustomRuleExists(Long id) {
        if (customRuleMapper.selectById(id) == null) {
            throw exception(CUSTOM_RULE_NOT_EXISTS);
        }
    }

    @Override
    public ErpCustomRuleDO getCustomRule(Long id) {
        return customRuleMapper.selectById(id);
    }

    @Override
    public PageResult<ErpCustomRuleDO> getCustomRulePage(ErpCustomRulePageReqVO pageReqVO) {
        return customRuleMapper.selectPage(pageReqVO);
    }

}