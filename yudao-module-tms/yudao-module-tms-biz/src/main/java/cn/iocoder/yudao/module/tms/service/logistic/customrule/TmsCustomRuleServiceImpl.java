package cn.iocoder.yudao.module.tms.service.logistic.customrule;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.enums.ErpDictTypeConstants;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.TmsCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.TmsCustomRulePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.TmsCustomRuleSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.TmsCustomProductDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.customrule.TmsCustomRuleDO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule.TmsCustomRuleMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.product.TmsCustomProductService;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.TmsCustomRuleBO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_BATCH_INSERT_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.ErrorCodeConstants.*;

/**
 * ERP 海关规则 Service 实现类
 *
 * @author 索迈管理员
 */
@Slf4j
@Service
@Validated
public class TmsCustomRuleServiceImpl implements TmsCustomRuleService {
    @Autowired
    TmsCustomRuleMapper customRuleMapper;
    @Resource
    MessageChannel erpCustomRuleChannel;
    @Autowired
    TmsCustomRuleApi tmsCustomRuleApi;
    @Autowired
    DictDataApi dictDataApi;
    @Autowired
    ErpProductApi erpProductApi;
    @Autowired
    TmsCustomProductService tmsCustomProductService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomRule(TmsCustomRuleSaveReqVO createReqVO) {
        baseValidator(createReqVO);
        //判断国别+产品编码是否重复
        createReqVO.getCountryCode().forEach(countryCode -> validateExist(null, countryCode, createReqVO.getProductId()));
        List<TmsCustomRuleDO> doList = createReqVO.getCountryCode().stream().map(countryCode -> copyPropertiesIgnoreType(createReqVO, countryCode)).toList();
        validProductHasCustomData(createReqVO);
        //批量添加
        ThrowUtil.ifThrow(!customRuleMapper.insertBatch(doList, doList.size()), DB_BATCH_INSERT_ERROR);
        //同步数据
        List<Long> ids = doList.stream().map(TmsCustomRuleDO::getId).toList();
        this.syncErpCustomRule(ids);
        // 返回
        return (long) ids.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomRule(TmsCustomRuleSaveReqVO updateReqVO) {
        baseValidator(updateReqVO);
        Long id = updateReqVO.getId();
        //判断国别+产品编码是否重复
        //获得updateReqVO国别的第一个
        Integer countryCode = updateReqVO.getCountryCode().stream().findFirst().orElseThrow(() -> exception(DB_UPDATE_ERROR));
        validateExist(id, countryCode, updateReqVO.getProductId());
        // 校验存在
        validateCustomRuleExists(id);
        validProductHasCustomData(updateReqVO);
        // 更新
        TmsCustomRuleDO updateObj = copyPropertiesIgnoreType(updateReqVO, countryCode);
        ThrowUtil.ifSqlThrow(customRuleMapper.updateById(updateObj), DB_UPDATE_ERROR);
        //同步数据
        this.syncErpCustomRule(Collections.singletonList(id));
    }

    //验证产品存在海关分类数据
    private void validProductHasCustomData(TmsCustomRuleSaveReqVO updateReqVO) {
//        ThrowUtil.ifThrow(erpProductApi.getProductDto(updateReqVO.getProductId()).getCustomCategoryId() == null, CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID);

        //根据产品id查询，如果不存在就异常
        TmsCustomProductDO customProduct = tmsCustomProductService.getCustomProductByProductId(updateReqVO.getProductId());
        if (customProduct == null) {
            throw exception(CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID);
        }
    }


    //同步海关规则方法
    private void syncErpCustomRule(List<Long> ruleIds) {
        List<TmsCustomRuleDTO> dtos = tmsCustomRuleApi.listCustomRules(ruleIds);
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
    public TmsCustomRuleDO getCustomRule(Long id) {
        return customRuleMapper.selectById(id);
    }

    @Override
    public PageResult<TmsCustomRuleDO> getCustomRulePage(TmsCustomRulePageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new TmsCustomRulePageReqVO();
        }
        return customRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public TmsCustomRuleBO getCustomRuleBOById(@NotNull Long id) {
        //检查规则下的产品是否存在海关分类
//        ThrowUtil.ifThrow(customRuleMapper.selectById(id) == null,CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID);
        TmsCustomRuleDO ruleDO = customRuleMapper.selectById(id);
        if (ruleDO == null) return null;
        if (erpProductApi.getProductDto(ruleDO.getProductId()).getCustomCategoryId() == null) {
            //不存在海关规则->原始table
            return BeanUtils.toBean(ruleDO, TmsCustomRuleBO.class);
        } else {
            //存在->关联查询bo
            return customRuleMapper.getCustomRuleBOById(id);
        }

    }

    @Override
    public List<TmsCustomRuleBO> getCustomRuleBOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return customRuleMapper.selectCustomRuleBOByIds(ids);
    }

    @Override
    public PageResult<TmsCustomRuleBO> getCustomRuleBOPage(TmsCustomRulePageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new TmsCustomRulePageReqVO();
        }
        return customRuleMapper.selectBOPage(pageReqVO);
    }

    private void validateExist(Long id, Integer countryCode, Long productId) {
        //校验产品是否存在
        erpProductApi.validProductList(Collections.singleton(productId));
        TmsCustomRuleDO tmsCustomRuleDO = customRuleMapper.getCustomRuleByCountryCodeAndProductId(countryCode, productId);
        if (tmsCustomRuleDO == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        String barCode = erpProductApi.getProductDto(productId).getBarCode();
        String countryDesc = dictDataApi.getDictDataLabel(DictTypeConstants.COUNTRY_CODE, countryCode);
        ThrowUtil.ifThrow(id == null, NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE, barCode + countryDesc);
        ThrowUtil.ifThrow(!tmsCustomRuleDO.getId().equals(id), NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE, barCode + countryDesc);
    }

    private void baseValidator(TmsCustomRuleSaveReqVO vo) {
        //国家
        Optional.ofNullable(vo.getCountryCode())
            .ifPresent(countryCodes -> dictDataApi.validateDictDataList(DictTypeConstants.COUNTRY_CODE,
                countryCodes.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toSet())));
        //货币
        Optional.ofNullable(vo.getDeclaredValueCurrencyCode()).ifPresent(i -> dictDataApi.validateDictDataList(DictTypeConstants.CURRENCY_CODE, Collections.singleton(String.valueOf(i))));
        //物流属性
        Optional.ofNullable(vo.getLogisticAttribute()).ifPresent(i -> dictDataApi.validateDictDataList(ErpDictTypeConstants.ERP_LOGISTIC_ATTRIBUTE, Collections.singleton(String.valueOf(i))));

    }

    /**
     * 自定义转换VO->DO。比较特殊。
     */
    protected TmsCustomRuleDO copyPropertiesIgnoreType(TmsCustomRuleSaveReqVO source, Integer countryCode) {
        //手动映射
        source.setCountryCode(null);
        return BeanUtils.toBean(source, TmsCustomRuleDO.class).setCountryCode(countryCode);
    }
}