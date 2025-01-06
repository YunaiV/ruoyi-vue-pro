package cn.iocoder.yudao.module.erp.service.logistic.customrule;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_INSERT_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 海关规则 Service 实现类
 *
 * @author 索迈管理员
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ErpCustomRuleServiceImpl implements ErpCustomRuleService {
    @Resource
    private MessageChannel erpCustomRuleChannel;

    private final ErpCustomRuleMapper customRuleMapper;


    @Override
    public Long createCustomRule(ErpCustomRuleSaveReqVO createReqVO) {
        //判断国别+供应商产品编码是否重复
        validateCountryCodeAndSupplierProductIdExist(null,createReqVO.getCountryCode(),createReqVO.getSupplierProductId());
        // 插入
        ErpCustomRuleDO customRule = BeanUtils.toBean(createReqVO, ErpCustomRuleDO.class);
        ThrowUtil.ifSqlThrow(customRuleMapper.insert(customRule),DB_INSERT_ERROR);
        Long id = customRule.getId();
        //同步数据
        var dtos = customRuleMapper.selectProductAllInfoListByCustomRuleId(id);
        erpCustomRuleChannel.send(MessageBuilder.withPayload(dtos).build());
        // 返回
        return id;
    }

    @Override
    public void updateCustomRule(ErpCustomRuleSaveReqVO updateReqVO) {
        Long id = updateReqVO.getId();
        //判断国别+供应商产品编码是否重复
        validateCountryCodeAndSupplierProductIdExist(id,updateReqVO.getCountryCode(),updateReqVO.getSupplierProductId());
        // 校验存在
        validateCustomRuleExists(id);
        // 更新
        ErpCustomRuleDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomRuleDO.class);
        ThrowUtil.ifSqlThrow(customRuleMapper.updateById(updateObj),DB_UPDATE_ERROR);
        //同步数据
        var dtos = customRuleMapper.selectProductAllInfoListByCustomRuleId(id);
        erpCustomRuleChannel.send(MessageBuilder.withPayload(dtos).build());
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

    private void validateCountryCodeAndSupplierProductIdExist(Long id, Integer countryCode, Long supplierProductId) {
        ErpCustomRuleDO erpCustomRuleDO = customRuleMapper.selectByCountryCodeAndSupplierProductId(countryCode, supplierProductId);
        if (erpCustomRuleDO == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw exception(NO_REPEAT_OF_COUNTRY_CODE_AND_SUPPLIER_PRODUCT_CODE);
        }
        if (!erpCustomRuleDO.getId().equals(id)) {
            throw exception(NO_REPEAT_OF_COUNTRY_CODE_AND_SUPPLIER_PRODUCT_CODE);
        }
    }

}